package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryAvailabilityRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryPricingRuleRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryReservationRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryRoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ReservationServiceTest {

    private val reservationRepository: ReservationRepository = InMemoryReservationRepository()
    private val roomRepository: RoomRepository = InMemoryRoomRepository()
    private val pricingRuleRepository: PricingRuleRepository = InMemoryPricingRuleRepository()
    private val availabilityRepository: AvailabilityRepository = InMemoryAvailabilityRepository()
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)
    private val service = ReservationService(
        reservationRepository,
        roomRepository,
        pricingRuleRepository,
        availabilityRepository,
        fixedClock
    )

    @Test
    fun `creates reservation and decrements availability`() {
        val roomId = RoomId.generate()
        val room = Room(
            id = roomId,
            hotelId = HotelId.generate(),
            number = RoomNumber("101"),
            type = RoomType.DOUBLE,
            baseRate = Money.of("USD", BigDecimal("100.00"))
        )
        roomRepository.save(room)

        val stay = DateRange(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 2, 3))
        val availability = Availability(
            id = AvailabilityId.generate(),
            roomId = roomId,
            range = stay,
            available = AvailableQuantity(2)
        )
        availabilityRepository.save(availability)

        val pricingRule = PricingRule(
            id = PricingRuleId("seasonal"),
            roomId = roomId,
            range = stay,
            price = Money.of("USD", BigDecimal("120.00")),
            description = null
        )
        pricingRuleRepository.save(pricingRule)

        val reservation = service.createReservation(UserId.generate(), roomId, stay)

        assertThat(reservation.status).isEqualTo(ReservationStatus.PENDING)
        assertThat(reservation.totalAmount.amount).isEqualByComparingTo(BigDecimal("240.00"))
        assertThat(availabilityRepository.findByRoomId(roomId).first().available.value).isEqualTo(1)
        assertThat(reservation.createdAt).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"))
    }

    @Test
    fun `uses most specific overlapping pricing rule`() {
        val roomId = RoomId.generate()
        val room = Room(
            id = roomId,
            hotelId = HotelId.generate(),
            number = RoomNumber("102"),
            type = RoomType.SINGLE,
            baseRate = Money.of("USD", BigDecimal("80.00"))
        )
        roomRepository.save(room)

        val stay = DateRange(LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 12))
        availabilityRepository.save(
            Availability(
                id = AvailabilityId.generate(),
                roomId = roomId,
                range = stay,
                available = AvailableQuantity(1)
            )
        )

        // broader rule
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("broad"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31)),
                price = Money.of("USD", BigDecimal("90.00")),
                description = null
            )
        )
        // more specific rule starting later
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("specific"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 9), LocalDate.of(2024, 3, 15)),
                price = Money.of("USD", BigDecimal("95.00")),
                description = null
            )
        )

        val reservation = service.createReservation(UserId.generate(), roomId, stay)

        assertThat(reservation.totalAmount.amount).isEqualByComparingTo(BigDecimal("190.00"))
    }
}
