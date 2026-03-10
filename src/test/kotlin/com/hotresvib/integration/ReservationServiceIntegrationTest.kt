package com.hotresvib.integration

import com.hotresvib.application.service.PriceCalculationService
import com.hotresvib.application.service.ReservationApplicationService
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryAvailabilityRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryPaymentRepository
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

class ReservationServiceIntegrationTest {

    private val reservationRepository = InMemoryReservationRepository()
    private val roomRepository = InMemoryRoomRepository()
    private val pricingRuleRepository = InMemoryPricingRuleRepository()
    private val availabilityRepository = InMemoryAvailabilityRepository()
    private val priceCalculationService = PriceCalculationService(roomRepository, pricingRuleRepository)
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-05-01T00:00:00Z"), ZoneOffset.UTC)

    private val service = ReservationApplicationService(
        reservationRepository,
        roomRepository,
        pricingRuleRepository,
        priceCalculationService,
        availabilityRepository,
        InMemoryPaymentRepository(),
        fixedClock
    )

    @Test
    fun `reservation persists with straddled pricing rules`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("201"),
                type = RoomType.SUITE,
                baseRate = Money.of("USD", BigDecimal("150.00"))
            )
        )

        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("june-start"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 12)),
                price = Money.of("USD", BigDecimal("180.00")),
                description = null
            )
        )
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("june-peak"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 6, 12), LocalDate.of(2024, 6, 15)),
                price = Money.of("USD", BigDecimal("220.00")),
                description = null
            )
        )

        val stay = DateRange(LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 15))
        val reservation = service.createReservation(UserId.generate(), roomId, stay)

        val persisted = reservationRepository.findById(reservation.id)
        assertThat(persisted).isNotNull
        assertThat(persisted!!.status).isEqualTo(ReservationStatus.DRAFT)
        assertThat(persisted.totalAmount.amount).isEqualByComparingTo(BigDecimal("1020.00"))
        assertThat(persisted.createdAt).isEqualTo(Instant.parse("2024-05-01T00:00:00Z"))
        assertThat(persisted.id).isNotEqualTo(ReservationId.generate())
    }

    @Test
    fun `cancelling reservation persists cancelled status`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("202"),
                type = RoomType.SUITE,
                baseRate = Money.of("USD", BigDecimal("200.00"))
            )
        )

        val reservation = service.createReservation(
            UserId.generate(),
            roomId,
            DateRange(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 4))
        )

        val cancelled = service.cancelReservation(reservation.id)

        assertThat(cancelled.status).isEqualTo(ReservationStatus.CANCELLED)
        assertThat(reservationRepository.findById(reservation.id)?.status).isEqualTo(ReservationStatus.CANCELLED)
        assertThat(cancelled.totalAmount.amount).isEqualByComparingTo(BigDecimal("600.00"))
    }
}
