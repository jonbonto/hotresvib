package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.pricing.PricingRule
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
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository,
    private val pricingRuleRepository: PricingRuleRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val clock: Clock = Clock.systemUTC()
    ) {

    fun createReservation(userId: UserId, roomId: RoomId, stay: DateRange): Reservation {
        require(stay.startDate.isBefore(stay.endDate)) { "Stay must be at least one night" }

        val room = roomRepository.findById(roomId) ?: throw IllegalArgumentException("Room not found")

        val availabilityByDate = availabilityByDate(roomId, stay)

        val coveringAvailability = mutableSetOf<Availability>()
        var cursor = stay.startDate
        while (cursor.isBefore(stay.endDate)) {
            val match = availabilityByDate[cursor]
            require(match != null) { "No availability for the selected dates" }
            require(match.available.value > 0) { "No availability for the selected dates" }
            coveringAvailability.add(match)
            cursor = cursor.plusDays(1)
        }

        val nights = ChronoUnit.DAYS.between(stay.startDate, stay.endDate)
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

    fun cancelReservation(reservationId: ReservationId): Reservation {
        val reservation = reservationRepository.findById(reservationId)
            ?: throw IllegalArgumentException("Reservation not found")
        require(reservation.status != ReservationStatus.CANCELLED) { "Reservation already cancelled" }
        require(reservation.status == ReservationStatus.PENDING) { "Only pending reservations can be cancelled" }

        val stay = reservation.stay
        val availabilityByDate = availabilityByDate(reservation.roomId, stay, "No availability records for reservation dates")

        val coveringAvailability = mutableSetOf<Availability>()
        var cursor = stay.startDate
        while (cursor.isBefore(stay.endDate)) {
            val match = availabilityByDate[cursor]
            require(match != null) { "No availability records for reservation dates" }
            coveringAvailability.add(match)
            cursor = cursor.plusDays(1)
        }

        coveringAvailability.forEach { availability ->
            availabilityRepository.save(
                availability.copy(
                    available = AvailableQuantity(availability.available.value + 1)
                )
            )
        }

        val cancelledReservation = reservation.copy(status = ReservationStatus.CANCELLED)
        reservationRepository.save(cancelledReservation)
        return cancelledReservation
    }

    private fun availabilityByDate(roomId: RoomId, stay: DateRange, missingMessage: String = "No availability for the selected dates"): Map<LocalDate, Availability> {
        val overlappingAvailability = availabilityRepository.findByRoomId(roomId)
            .filter { it.range.overlaps(stay) }
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
