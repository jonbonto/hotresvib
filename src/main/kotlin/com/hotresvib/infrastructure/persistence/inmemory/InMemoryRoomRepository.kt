package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryRoomRepository : RoomRepository {
    private val storage = ConcurrentHashMap<RoomId, Room>()

    override fun findById(id: RoomId): Room? = storage[id]

    override fun findByHotelId(hotelId: HotelId): List<Room> =
        storage.values.filter { it.hotelId == hotelId }

    override fun save(room: Room): Room {
        storage[room.id] = room
        return room
    }
}
