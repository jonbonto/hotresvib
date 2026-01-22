package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val pricingRuleRepository: PricingRuleRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val clock: Clock = Clock.systemUTC()
    ) {

    fun createReservation(userId: UserId, roomId: RoomId, stay: DateRange): Reservation {
        require(stay.start.isBefore(stay.end)) { "Stay must be at least one night" }

        val room = roomRepository.findById(roomId) ?: throw IllegalArgumentException("Room not found")

        val overlappingAvailability = availabilityRepository.findByRoomId(roomId)
            .filter { it.range.overlapsHalfOpen(stay) }
        require(overlappingAvailability.isNotEmpty()) { "No availability for the selected dates" }

        val coveringAvailability = mutableSetOf<com.hotresvib.domain.availability.Availability>()
        var cursor = stay.start
        while (cursor.isBefore(stay.end)) {
            val match = overlappingAvailability.firstOrNull { !cursor.isBefore(it.range.start) && cursor.isBefore(it.range.end) }
            require(match != null) { "No availability for the selected dates" }
            require(match.available.value > 0) { "No availability for the selected dates" }
            coveringAvailability.add(match)
            cursor = cursor.plusDays(1)
        }

        val nights = ChronoUnit.DAYS.between(stay.start, stay.end)
        val applicableRate = pricingRuleRepository.findByRoomId(roomId)
            .filter { it.range.overlapsHalfOpen(stay) }
            .minWithOrNull(
                compareByDescending<com.hotresvib.domain.pricing.PricingRule> { it.range.start }
                    .thenBy { it.range.end }
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
            status = ReservationStatus.PENDING,
            createdAt = Instant.now(clock)
        )

        reservationRepository.save(reservation)
        coveringAvailability.forEach { availability ->
            availabilityRepository.save(
                availability.copy(
                    available = AvailableQuantity(availability.available.value - 1)
                )
            )
        }

        return reservation
    }
}
