package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.BlockoutReason
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDate
import java.util.UUID

class AvailabilityServiceTest {

    private lateinit var availabilityRepository: AvailabilityRepository
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var service: AvailabilityApplicationService

    @BeforeEach
    fun setup() {
        availabilityRepository = mock(AvailabilityRepository::class.java)
        reservationRepository = mock(ReservationRepository::class.java)
        service = AvailabilityApplicationService(availabilityRepository, reservationRepository)
    }

    @Test
    fun `should return true when there are no reservation conflicts or blockouts`() {
        val roomId = RoomId(UUID.randomUUID())
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)

        `when`(reservationRepository.hasConflict(roomId, DateRange(startDate, endDate), setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)))
            .thenReturn(false)
        `when`(availabilityRepository.findByRoomId(roomId)).thenReturn(emptyList())

        val result = service.checkAvailability(roomId, startDate, endDate)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when a blockout overlaps the requested stay`() {
        val roomId = RoomId(UUID.randomUUID())
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)

        `when`(reservationRepository.hasConflict(roomId, DateRange(startDate, endDate), setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)))
            .thenReturn(false)
        `when`(availabilityRepository.findByRoomId(roomId)).thenReturn(
            listOf(
                Availability(
                    id = AvailabilityId.generate(),
                    roomId = roomId,
                    range = DateRange(LocalDate.of(2026, 2, 3), LocalDate.of(2026, 2, 4)),
                    reason = BlockoutReason("MAINTENANCE")
                )
            )
        )

        val result = service.checkAvailability(roomId, startDate, endDate)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when there is an active reservation conflict`() {
        val roomId = RoomId(UUID.randomUUID())
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)

        `when`(reservationRepository.hasConflict(roomId, DateRange(startDate, endDate), setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)))
            .thenReturn(true)

        val result = service.checkAvailability(roomId, startDate, endDate)

        assertThat(result).isFalse()
    }
}
