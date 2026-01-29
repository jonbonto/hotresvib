package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.*
import java.time.LocalDate
import java.util.UUID

class AvailabilityServiceTest {

    private lateinit var repository: AvailabilityRepository
    private lateinit var service: AvailabilityApplicationService

    @BeforeEach
    fun setup() {
        repository = mock(AvailabilityRepository::class.java)
        service = AvailabilityApplicationService(repository)
    }

    @Test
    fun `should return true when room is available for entire date range`() {
        val roomId = RoomId(UUID.randomUUID())
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)
        
        val availability = listOf(
            Availability(AvailabilityId.generate(), roomId, DateRange(startDate, LocalDate.of(2026, 2, 3)), AvailableQuantity(5)),
            Availability(AvailabilityId.generate(), roomId, DateRange(LocalDate.of(2026, 2, 3), endDate), AvailableQuantity(5))
        )

        `when`(repository.findByRoomId(roomId)).thenReturn(availability)

        val result = service.checkAvailability(roomId, startDate, endDate)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when room has availability but quantity is zero`() {
        val roomId = RoomId(UUID.randomUUID())
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)
        
        val availability = listOf(
            Availability(AvailabilityId.generate(), roomId, DateRange(startDate, endDate), AvailableQuantity(0))
        )

        `when`(repository.findByRoomId(roomId)).thenReturn(availability)

        val result = service.checkAvailability(roomId, startDate, endDate)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return true when room has no overlapping availability`() {
        val roomId = RoomId(UUID.randomUUID())
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)

        `when`(repository.findByRoomId(roomId)).thenReturn(emptyList())

        val result = service.checkAvailability(roomId, startDate, endDate)

        // Empty list: all() returns true on empty collection in Kotlin
        assertThat(result).isTrue()
    }
}
