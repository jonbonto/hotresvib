package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.BlockoutReason
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
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryPaymentRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryPricingRuleRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryReservationRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryRoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
    private val priceCalculationService = PriceCalculationService(roomRepository, pricingRuleRepository)
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)
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
    fun `creates reservation with per-night pricing across multiple rules`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("101"),
                type = RoomType.DOUBLE,
                baseRate = Money.of("USD", BigDecimal("100.00"))
            )
        )

        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("early-march"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 4)),
                price = Money.of("USD", BigDecimal("120.00")),
                description = "Shoulder season"
            )
        )
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("holiday"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 7)),
                price = Money.of("USD", BigDecimal("150.00")),
                description = "Holiday period"
            )
        )

        val stay = DateRange(LocalDate.of(2024, 3, 2), LocalDate.of(2024, 3, 8))

        val reservation = service.createReservation(UserId.generate(), roomId, stay)

        assertThat(reservation.status).isEqualTo(ReservationStatus.DRAFT)
        assertThat(reservation.totalAmount.amount).isEqualByComparingTo(BigDecimal("790.00"))
        assertThat(reservation.createdAt).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"))
    }

    @Test
    fun `uses the latest matching rule start date for overlapping nightly rules`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("102"),
                type = RoomType.SINGLE,
                baseRate = Money.of("USD", BigDecimal("80.00"))
            )
        )

        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("broad"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31)),
                price = Money.of("USD", BigDecimal("90.00")),
                description = null
            )
        )
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("specific"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 9), LocalDate.of(2024, 3, 15)),
                price = Money.of("USD", BigDecimal("95.00")),
                description = null
            )
        )

        val reservation = service.createReservation(
            UserId.generate(),
            roomId,
            DateRange(LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 12))
        )

        assertThat(reservation.totalAmount.amount).isEqualByComparingTo(BigDecimal("190.00"))
    }

    @Test
    fun `fails when a blockout overlaps the requested stay`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("103"),
                type = RoomType.SINGLE,
                baseRate = Money.of("USD", BigDecimal("50.00"))
            )
        )

        availabilityRepository.save(
            Availability(
                id = AvailabilityId.generate(),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 4, 2), LocalDate.of(2024, 4, 3)),
                reason = BlockoutReason("MAINTENANCE")
            )
        )

        assertThatThrownBy {
            service.createReservation(
                UserId.generate(),
                roomId,
                DateRange(LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 4))
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("No availability for the selected dates")
    }

    @Test
    fun `cancelling reservation marks it cancelled`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("104"),
                type = RoomType.DOUBLE,
                baseRate = Money.of("USD", BigDecimal("110.00"))
            )
        )

        val reservation = service.createReservation(
            UserId.generate(),
            roomId,
            DateRange(LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 3))
        )

        val cancelled = service.cancelReservation(reservation.id)

        assertThat(cancelled.status).isEqualTo(ReservationStatus.CANCELLED)
        assertThat(reservationRepository.findById(reservation.id)?.status).isEqualTo(ReservationStatus.CANCELLED)
        assertThat(cancelled.totalAmount.amount).isEqualByComparingTo(BigDecimal("220.00"))
    }

    @Test
    fun `cancelled reservation no longer blocks availability conflict checks`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("105"),
                type = RoomType.DOUBLE,
                baseRate = Money.of("USD", BigDecimal("120.00"))
            )
        )

        val stay = DateRange(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 4))
        val reservation = service.createReservation(UserId.generate(), roomId, stay)
        val pending = service.initiatePayment(reservation.id)

        val activeStatuses = setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        assertThat(reservationRepository.hasConflict(roomId, stay, activeStatuses)).isTrue()

        service.cancelReservation(pending.id)

        assertThat(reservationRepository.hasConflict(roomId, stay, activeStatuses)).isFalse()
    }

    @Test
    fun `expired reservation no longer blocks availability conflict checks`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("106"),
                type = RoomType.SINGLE,
                baseRate = Money.of("USD", BigDecimal("90.00"))
            )
        )

        val stay = DateRange(LocalDate.of(2024, 7, 10), LocalDate.of(2024, 7, 12))
        val reservation = service.createReservation(UserId.generate(), roomId, stay)
        val pending = service.initiatePayment(reservation.id)

        val activeStatuses = setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        assertThat(reservationRepository.hasConflict(roomId, stay, activeStatuses)).isTrue()

        service.expireReservation(pending.id)

        assertThat(reservationRepository.hasConflict(roomId, stay, activeStatuses)).isFalse()
    }
}
