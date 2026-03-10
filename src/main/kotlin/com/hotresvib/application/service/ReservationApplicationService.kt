package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class ReservationApplicationService(
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val pricingRuleRepository: PricingRuleRepository,
    private val priceCalculationService: PriceCalculationService,
    private val availabilityRepository: AvailabilityRepository,
    private val paymentRepository: PaymentRepository,
    private val clock: Clock = Clock.systemUTC()
) {

    companion object {
        private const val MAX_STAY_DURATION_NIGHTS = 30L
        private const val DEFAULT_CHECKIN_HOUR = 15
    }

    @Transactional
    fun createReservation(userId: UserId, roomId: RoomId, stay: DateRange): Reservation {
        require(stay.startDate.isBefore(stay.endDate)) { "Stay must be at least one night" }

        val nights = ChronoUnit.DAYS.between(stay.startDate, stay.endDate)
        require(nights >= 1) { "Minimum stay duration is 1 night" }
        require(nights <= MAX_STAY_DURATION_NIGHTS) { "Maximum stay duration is $MAX_STAY_DURATION_NIGHTS nights" }

        val today = LocalDate.now(clock)
        require(!stay.startDate.isBefore(today)) { "Check-in date must be in the future" }

        if (stay.startDate.isEqual(today)) {
            val zoneId = ZoneId.of("UTC")
            val currentHour = java.time.LocalTime.now(zoneId).hour
            require(currentHour < DEFAULT_CHECKIN_HOUR) {
                "Same-day booking not available. Check-in time is $DEFAULT_CHECKIN_HOUR PM"
            }
        }

        val room = roomRepository.findById(roomId) ?: throw IllegalArgumentException("Room not found")
        val activeBlockingStatuses = setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        if (reservationRepository.hasConflict(roomId, stay, activeBlockingStatuses)) {
            throw IllegalArgumentException("No availability for the selected dates")
        }
        val overlappingBlockouts = availabilityRepository.findByRoomId(roomId)
            .filter { it.range.overlaps(stay) }
        if (overlappingBlockouts.isNotEmpty()) {
            throw IllegalArgumentException("No availability for the selected dates")
        }

        val pricingRules = pricingRuleRepository.findByRoomId(roomId)
        val totalAmount = priceCalculationService.calculateTotalAmount(room, stay, pricingRules)

        val reservation = Reservation(
            id = ReservationId.generate(),
            userId = userId,
            roomId = roomId,
            stay = stay,
            totalAmount = totalAmount,
            status = ReservationStatus.DRAFT,
            createdAt = Instant.now(clock)
        )

        return reservationRepository.save(reservation)
    }

    @Transactional
    fun initiatePayment(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")

        validateStateTransition(reservation.status, ReservationStatus.PENDING_PAYMENT)
        return reservationRepository.save(reservation.copy(status = ReservationStatus.PENDING_PAYMENT))
    }

    @Transactional
    fun confirmPayment(reservationId: ReservationId, paymentId: UUID): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")

        validateStateTransition(reservation.status, ReservationStatus.CONFIRMED)

        val payment = paymentRepository.findById(paymentId)
        if (payment != null) {
            paymentRepository.save(payment.copy(status = PaymentStatus.COMPLETED))
        }

        return reservationRepository.save(reservation.copy(status = ReservationStatus.CONFIRMED))
    }

    @Transactional
    fun expireReservation(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")

        validateStateTransition(reservation.status, ReservationStatus.EXPIRED)

        return reservationRepository.save(reservation.copy(status = ReservationStatus.EXPIRED))
    }

    @Transactional
    fun cancelReservation(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found")

        validateStateTransition(reservation.status, ReservationStatus.CANCELLED)

        return reservationRepository.save(reservation.copy(status = ReservationStatus.CANCELLED))
    }

    @Transactional
    fun refundReservation(reservationId: ReservationId, refundId: String): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found: $reservationId")

        validateStateTransition(reservation.status, ReservationStatus.REFUNDED)

        val payments = paymentRepository.findByReservationId(reservationId)
        payments.forEach { payment ->
            if (payment.status == PaymentStatus.COMPLETED) {
                paymentRepository.save(
                    payment.copy(
                        status = PaymentStatus.REFUNDED,
                        transactionId = refundId
                    )
                )
            }
        }

        return reservationRepository.save(reservation.copy(status = ReservationStatus.REFUNDED))
    }

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

}
