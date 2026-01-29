package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AvailabilityApplicationService(
    private val availabilityRepository: AvailabilityRepository
) {
    fun checkAvailability(roomId: RoomId, startDate: LocalDate, endDate: LocalDate): Boolean {
        val range = DateRange(startDate, endDate)
        val availabilities = availabilityRepository.findByRoomId(roomId)
            .filter { it.range.overlaps(range) }
        return availabilities.all { it.available.value > 0 }
    }

    fun updateAvailability(availability: Availability) {
        availabilityRepository.save(availability)
    }
}
