package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AvailabilityApplicationService(
    private val availabilityRepository: AvailabilityRepository,
    private val reservationRepository: ReservationRepository
) {
    fun checkAvailability(roomId: RoomId, startDate: LocalDate, endDate: LocalDate): Boolean {
        val range = DateRange(startDate, endDate)
        val activeBlockingStatuses = setOf(ReservationStatus.CONFIRMED, ReservationStatus.PENDING_PAYMENT)
        if (reservationRepository.hasConflict(roomId, range, activeBlockingStatuses)) {
            return false
        }

        val overlappingBlockouts = availabilityRepository.findByRoomId(roomId)
            .filter { it.range.overlaps(range) }
        return overlappingBlockouts.isEmpty()
    }

    fun updateAvailability(availability: Availability) {
        availabilityRepository.save(availability)
    }
}
