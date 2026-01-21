package com.hotresvib.application.port

import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.RoomId

interface AvailabilityRepository {
    fun findByRoomId(roomId: RoomId): List<Availability>
    fun save(availability: Availability): Availability
}
