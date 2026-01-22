package com.hotresvib.integration

import com.hotresvib.application.service.ReservationService
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
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

class ReservationServiceIntegrationTest {

    private val reservationRepository = InMemoryReservationRepository()
    private val roomRepository = InMemoryRoomRepository()
    private val pricingRuleRepository = InMemoryPricingRuleRepository()
    private val availabilityRepository = InMemoryAvailabilityRepository()
    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-05-01T00:00:00Z"), ZoneOffset.UTC)

    private val service = ReservationService(
        reservationRepository,
        roomRepository,
        pricingRuleRepository,
        availabilityRepository,
        fixedClock
    )

    @Test
    fun `reservation persists and decrements availability`() {
        val roomId = RoomId.generate()
        val room = Room(
            id = roomId,
            hotelId = HotelId.generate(),
            number = RoomNumber("201"),
            type = RoomType.SUITE,
            baseRate = Money.of("USD", BigDecimal("150.00"))
        )
        roomRepository.save(room)

        val stay = DateRange(LocalDate.of(2024, 6, 10), LocalDate.of(2024, 6, 12))
        availabilityRepository.save(
            Availability(
                id = AvailabilityId.generate(),
                roomId = roomId,
                range = stay,
                available = AvailableQuantity(1)
            )
        )

        val userId = UserId.generate()
        val reservation = service.createReservation(userId, roomId, stay)

        val persisted = reservationRepository.findById(reservation.id)
        assertThat(persisted).isNotNull
        assertThat(persisted!!.status).isEqualTo(ReservationStatus.PENDING_PAYMENT)
        assertThat(persisted.totalAmount.amount).isEqualByComparingTo(BigDecimal("300.00"))
        assertThat(persisted.createdAt).isEqualTo(Instant.parse("2024-05-01T00:00:00Z"))

        val remainingAvailability = availabilityRepository.findByRoomId(roomId).first()
        assertThat(remainingAvailability.available.value).isZero()

        // sanity check that original id remains unchanged
        assertThat(persisted.id).isNotEqualTo(ReservationId.generate())
    }

    @Test
    fun `cancelling reservation restores availability`() {
        val roomId = RoomId.generate()
        val room = Room(
            id = roomId,
            hotelId = HotelId.generate(),
            number = RoomNumber("202"),
            type = RoomType.SUITE,
            baseRate = Money.of("USD", BigDecimal("200.00"))
        )
        roomRepository.save(room)

        val stay = DateRange(LocalDate.of(2024, 7, 1), LocalDate.of(2024, 7, 4))
        availabilityRepository.save(
            Availability(
                id = AvailabilityId.generate(),
                roomId = roomId,
                range = stay,
                available = AvailableQuantity(1)
            )
        )

        val reservation = service.createReservation(UserId.generate(), roomId, stay)
        assertThat(availabilityRepository.findByRoomId(roomId).first().available.value).isZero()

        val cancelled = service.cancelReservation(reservation.id)

        assertThat(cancelled.status).isEqualTo(ReservationStatus.CANCELLED)
        assertThat(availabilityRepository.findByRoomId(roomId).first().available.value).isEqualTo(1)
    }
}
