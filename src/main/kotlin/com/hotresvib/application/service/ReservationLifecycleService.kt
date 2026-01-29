package com.hotresvib.application.service

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.shared.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
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
        
        // TODO: Hold availability (reduce available quantity by 1)
        // This would require updating the Availability entity
        
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
        
        // Release availability
        // TODO: Restore available quantity
        
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
        
        // Release availability
        // TODO: Restore available quantity
        
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
            ReservationStatus.DRAFT to setOf(ReservationStatus.PENDING_PAYMENT, ReservationStatus.CANCELLED),
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
}
