package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryAvailabilityRepository : AvailabilityRepository {
    private val storage = ConcurrentHashMap<RoomId, MutableList<Availability>>()
    private val lock = Any()

    override fun findByRoomId(roomId: RoomId): List<Availability> =
        synchronized(lock) {
            storage[roomId]?.toList().orEmpty()
        }

    override fun save(availability: Availability): Availability {
        synchronized(lock) {
            storage.compute(availability.roomId) { _, existing ->
                val updated = existing ?: mutableListOf()
                val indexById = updated.indexOfFirst { it.id == availability.id }
                val indexByRange = updated.indexOfFirst { it.range == availability.range }
                val index = if (indexById >= 0) indexById else indexByRange
                val overlaps = updated.withIndex().any { (currentIndex, existingAvailability) ->
                    currentIndex != index && rangesOverlap(existingAvailability.range, availability.range)
                }
                require(!overlaps) { "Availability ranges cannot overlap for the same room" }
                if (index >= 0) {
                    updated[index] = availability
                } else {
                    updated.add(availability)
                }
                updated
            }
        }
        return availability
    }

    private fun rangesOverlap(left: DateRange, right: DateRange): Boolean {
        return left.overlapsHalfOpen(right)
    }
}
