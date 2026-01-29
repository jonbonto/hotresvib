package com.hotresvib.application.port

import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.RoomId

interface AvailabilityRepository {
    fun findByRoomId(roomId: RoomId): List<Availability>
    fun save(availability: Availability): Availability
    fun findByRoomIdAndDateRange(roomId: RoomId, startDate: java.time.LocalDate, endDate: java.time.LocalDate): List<Availability> {
        return findByRoomId(roomId).filter { it.range.overlaps(com.hotresvib.domain.shared.DateRange(startDate, endDate)) }
    }

    fun findByRoomIdAndDateRange(roomId: java.util.UUID, startDate: java.time.LocalDate, endDate: java.time.LocalDate): List<Availability> {
        return findByRoomIdAndDateRange(RoomId(roomId), startDate, endDate)
    }
}
