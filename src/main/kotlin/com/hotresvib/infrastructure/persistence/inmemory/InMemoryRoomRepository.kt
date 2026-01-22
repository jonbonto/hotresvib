package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryRoomRepository : RoomRepository {
    private val storage = ConcurrentHashMap<RoomId, Room>()
    private val lock = Any()

    override fun findById(id: RoomId): Room? =
        synchronized(lock) {
            storage[id]
        }

    override fun findByHotelId(hotelId: HotelId): List<Room> =
        synchronized(lock) {
            storage.values.filter { it.hotelId == hotelId }
        }

    override fun save(room: Room): Room {
        synchronized(lock) {
            storage[room.id] = room
        }
        return room
    }
}
