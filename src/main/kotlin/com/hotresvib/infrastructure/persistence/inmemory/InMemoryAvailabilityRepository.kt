package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryAvailabilityRepository : AvailabilityRepository {
    private val storage = ConcurrentHashMap<RoomId, MutableList<Availability>>()

    override fun findByRoomId(roomId: RoomId): List<Availability> =
        storage[roomId]?.toList().orEmpty()

    override fun save(availability: Availability): Availability {
        storage.compute(availability.roomId) { _, existing ->
            val updated = existing ?: mutableListOf()
            val index = updated.indexOfFirst { it.range == availability.range }
            if (index >= 0) {
                updated[index] = availability
            } else {
                require(updated.none { rangesOverlap(it.range, availability.range) }) {
                    "Availability ranges cannot overlap for the same room"
                }
                updated.add(availability)
            }
            updated
        }
        return availability
    }

    private fun rangesOverlap(left: DateRange, right: DateRange): Boolean {
        return left.start.isBefore(right.end) && right.start.isBefore(left.end)
    }
}
