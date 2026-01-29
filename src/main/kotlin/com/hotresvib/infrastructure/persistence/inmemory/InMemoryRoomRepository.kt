package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryRoomRepository : RoomRepository {
    val rooms = ConcurrentHashMap<RoomId, Room>()
    private val lock = Any()

    override fun findById(id: RoomId): Room? =
        synchronized(lock) {
            rooms[id]
        }

    override fun findByHotelId(hotelId: HotelId): List<Room> =
        synchronized(lock) {
            rooms.values.filter { it.hotelId == hotelId }
        }

    override fun findAll(): List<Room> =
        synchronized(lock) {
            rooms.values.toList()
        }

    override fun save(room: Room): Room {
        synchronized(lock) {
            rooms[room.id] = room
        }
        return room
    }
}
