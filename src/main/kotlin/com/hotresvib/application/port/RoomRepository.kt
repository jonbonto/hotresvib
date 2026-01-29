package com.hotresvib.application.port

import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId

interface RoomRepository {
    fun findById(id: RoomId): Room?
    fun findByHotelId(hotelId: HotelId): List<Room>
    fun findAll(): List<Room>
    fun save(room: Room): Room
}
