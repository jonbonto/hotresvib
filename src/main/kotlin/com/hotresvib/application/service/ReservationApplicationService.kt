package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
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
        updateAvailabilityForStay(roomId, stay, delta = -1, lockForUpdate = true)

        val applicableRate = pricingRuleRepository.findByRoomId(roomId)
            .filter { it.range.overlaps(stay) }
            .minWithOrNull(
                compareByDescending<PricingRule> { it.range.startDate }
                    .thenBy { it.range.endDate }
            )
            ?.price ?: room.baseRate

        val totalAmount = Money(
            amount = applicableRate.amount.multiply(BigDecimal.valueOf(nights)),
            currency = applicableRate.currency
        )

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
        updateAvailabilityForStay(reservation.roomId, reservation.stay, delta = 1)

        return reservationRepository.save(reservation.copy(status = ReservationStatus.EXPIRED))
    }

    @Transactional
    fun cancelReservation(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found")

        validateStateTransition(reservation.status, ReservationStatus.CANCELLED)
        updateAvailabilityForStay(reservation.roomId, reservation.stay, delta = 1)

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

    private fun updateAvailabilityForStay(roomId: RoomId, stay: DateRange, delta: Int, lockForUpdate: Boolean = false) {
        val availabilityByDate = availabilityByDate(roomId, stay, lockForUpdate = lockForUpdate)

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

    private fun availabilityByDate(
        roomId: RoomId,
        stay: DateRange,
        missingMessage: String = "No availability for the selected dates",
        lockForUpdate: Boolean = false
    ): Map<LocalDate, Availability> {
        val roomAvailability = if (lockForUpdate) {
            availabilityRepository.findByRoomIdForUpdate(roomId)
        } else {
            availabilityRepository.findByRoomId(roomId)
        }

        val overlappingAvailability = roomAvailability.filter { it.range.overlaps(stay) }
        require(overlappingAvailability.isNotEmpty()) { missingMessage }

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
