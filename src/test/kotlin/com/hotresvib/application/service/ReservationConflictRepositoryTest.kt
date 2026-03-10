package com.hotresvib.application.service

import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryReservationRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class ReservationConflictRepositoryTest {

    private val repository = InMemoryReservationRepository()

    @Test
    fun `hasConflict returns true for overlapping active reservation`() {
        val roomId = RoomId.generate()
        seedReservation(
            roomId = roomId,
            stay = DateRange(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 14)),
            status = ReservationStatus.CONFIRMED
        )

        val hasConflict = repository.hasConflict(
            roomId,
            DateRange(LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 16)),
            setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        )

        assertThat(hasConflict).isTrue()
    }

    @Test
    fun `hasConflict returns false when ranges only touch at boundary`() {
        val roomId = RoomId.generate()
        seedReservation(
            roomId = roomId,
            stay = DateRange(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 14)),
            status = ReservationStatus.CONFIRMED
        )

        val hasConflict = repository.hasConflict(
            roomId,
            DateRange(LocalDate.of(2026, 9, 14), LocalDate.of(2026, 9, 16)),
            setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        )

        assertThat(hasConflict).isFalse()
    }

    @Test
    fun `hasConflict returns false when overlapping reservation has non-blocking status`() {
        val roomId = RoomId.generate()
        seedReservation(
            roomId = roomId,
            stay = DateRange(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 14)),
            status = ReservationStatus.DRAFT
        )

        val hasConflict = repository.hasConflict(
            roomId,
            DateRange(LocalDate.of(2026, 9, 12), LocalDate.of(2026, 9, 16)),
            setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        )

        assertThat(hasConflict).isFalse()
    }

    private fun seedReservation(roomId: RoomId, stay: DateRange, status: ReservationStatus) {
        repository.save(
            Reservation(
                id = ReservationId.generate(),
                userId = UserId.generate(),
                roomId = roomId,
                stay = stay,
                totalAmount = Money.of("USD", BigDecimal("200.00")),
                status = status,
                createdAt = Instant.now()
            )
        )
    }
}
