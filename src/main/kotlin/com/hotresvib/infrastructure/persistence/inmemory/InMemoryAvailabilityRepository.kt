package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryAvailabilityRepository : AvailabilityRepository {
    private val storage = ConcurrentHashMap<RoomId, MutableList<Availability>>()

    override fun findByRoomId(roomId: RoomId): List<Availability> =
        storage[roomId]?.toList().orEmpty()

    override fun save(availability: Availability): Availability {
        storage.compute(availability.roomId) { _, existing ->
            val updated = existing ?: mutableListOf()
            updated.add(availability)
            updated
        }
        return availability
    }
}
