package com.hotresvib.infrastructure.persistence.inmemory

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("demo")
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
                val index = updated.indexOfFirst { it.id == availability.id }
                val overlaps = updated.any {
                    it.id != availability.id && rangesOverlap(it.range, availability.range)
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
        return left.overlaps(right)
    }
}
