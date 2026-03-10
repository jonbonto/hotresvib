package com.hotresvib.application.service

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.shared.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Service
class ReservationLifecycleService(
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository,
    private val availabilityRepository: AvailabilityRepository
) {
    
    /**
     * Create a new reservation in DRAFT state
     * Holds availability for the room
     */
    @Transactional
    fun createDraft(
        userId: UserId,
        roomId: RoomId,
        stay: DateRange,
        totalAmount: Money
    ): Reservation {
        val reservation = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = userId,
            roomId = roomId,
            stay = stay,
            totalAmount = totalAmount,
            status = ReservationStatus.DRAFT,
            createdAt = Instant.now()
        )
        
        updateAvailabilityForStay(roomId, stay, delta = -1)
        
        return reservationRepository.save(reservation)
    }
    
    /**
     * Transition reservation to PENDING_PAYMENT
     * Called when payment intent is created
     */
    @Transactional
    fun initiatePayment(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")
        
        validateStateTransition(reservation.status, ReservationStatus.PENDING_PAYMENT)
        
        val updated = reservation.copy(status = ReservationStatus.PENDING_PAYMENT)
        return reservationRepository.save(updated)
    }
    
    /**
     * Confirm payment and transition to CONFIRMED state
     * Called by webhook when payment succeeds
     */
    @Transactional
    fun confirmPayment(reservationId: ReservationId, paymentId: UUID): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")
        
        validateStateTransition(reservation.status, ReservationStatus.CONFIRMED)
        
        // Update payment status
        val payment = paymentRepository.findById(paymentId)
        if (payment != null) {
            val updatedPayment = payment.copy(status = PaymentStatus.COMPLETED)
            paymentRepository.save(updatedPayment)
        }
        
        val updated = reservation.copy(status = ReservationStatus.CONFIRMED)
        return reservationRepository.save(updated)
    }
    
    /**
     * Expire a reservation that wasn't paid in time
     * Releases held availability
     */
    @Transactional
    fun expireReservation(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")
        
        validateStateTransition(reservation.status, ReservationStatus.EXPIRED)
        
        updateAvailabilityForStay(reservation.roomId, reservation.stay, delta = 1)
        
        val updated = reservation.copy(status = ReservationStatus.EXPIRED)
        return reservationRepository.save(updated)
    }
    
    /**
     * Cancel a confirmed reservation
     */
    @Transactional
    fun cancelReservation(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")
        
        validateStateTransition(reservation.status, ReservationStatus.CANCELLED)
        
        updateAvailabilityForStay(reservation.roomId, reservation.stay, delta = 1)
        
        val updated = reservation.copy(status = ReservationStatus.CANCELLED)
        return reservationRepository.save(updated)
    }
    
    /**
     * Process refund for cancelled reservation
     */
    @Transactional
    fun refundReservation(reservationId: ReservationId, refundId: String): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")
        
        validateStateTransition(reservation.status, ReservationStatus.REFUNDED)
        
        // Find and update payment
        val payments = paymentRepository.findByReservationId(reservationId)
        payments.forEach { payment ->
            if (payment.status == PaymentStatus.COMPLETED) {
                val refunded = payment.copy(
                    status = PaymentStatus.REFUNDED,
                    transactionId = refundId
                )
                paymentRepository.save(refunded)
            }
        }
        
        val updated = reservation.copy(status = ReservationStatus.REFUNDED)
        return reservationRepository.save(updated)
    }
    
    /**
     * Validate state transitions
     */
    private fun validateStateTransition(from: ReservationStatus, to: ReservationStatus) {
        val validTransitions = mapOf(
            ReservationStatus.DRAFT to setOf(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CANCELLED, ReservationStatus.EXPIRED),
            ReservationStatus.PENDING_PAYMENT to setOf(ReservationStatus.CONFIRMED, ReservationStatus.EXPIRED),
            ReservationStatus.CONFIRMED to setOf(ReservationStatus.CANCELLED),
            ReservationStatus.CANCELLED to setOf(ReservationStatus.REFUNDED),
            ReservationStatus.EXPIRED to emptySet(),
            ReservationStatus.REFUNDED to emptySet()
        )
        
        val allowed = validTransitions[from] ?: emptySet()
        if (!allowed.contains(to)) {
            throw IllegalStateException("Invalid state transition from $from to $to")
        }
    }

    private fun updateAvailabilityForStay(roomId: RoomId, stay: DateRange, delta: Int) {
        val availabilityByDate = availabilityByDate(roomId, stay)

        val coveringAvailability = mutableSetOf<Availability>()
        var cursor = stay.startDate
        while (cursor.isBefore(stay.endDate)) {
            val match = availabilityByDate[cursor]
            require(match != null) { "No availability for the selected dates" }
            if (delta < 0) {
                require(match.available.value > 0) { "No availability for the selected dates" }
            }
            coveringAvailability.add(match)
            cursor = cursor.plusDays(1)
        }

        coveringAvailability.forEach { availability ->
            val updatedQuantity = availability.available.value + delta
            require(updatedQuantity >= 0) { "Available quantity must be non-negative" }
            availabilityRepository.save(
                availability.copy(available = AvailableQuantity(updatedQuantity))
            )
        }
    }

    private fun availabilityByDate(roomId: RoomId, stay: DateRange): Map<LocalDate, Availability> {
        val overlappingAvailability = availabilityRepository.findByRoomId(roomId)
            .filter { it.range.overlaps(stay) }
        require(overlappingAvailability.isNotEmpty()) { "No availability for the selected dates" }

        return overlappingAvailability
            .flatMap { availability ->
                generateSequence(availability.range.startDate) { current ->
                    val next = current.plusDays(1)
                    if (next.isBefore(availability.range.endDate)) next else null
                }
                    .map { it to availability }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { entry ->
                require(entry.value.distinct().size == 1) { "Availability ranges overlap for the same date" }
                entry.value.first()
            }
    }
}
