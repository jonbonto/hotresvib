package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.infrastructure.persistence.jpa.RoomJpaRepository
import org.springframework.stereotype.Repository

@Repository
class RoomJpaAdapter(private val repo: RoomJpaRepository) : RoomRepository {
    override fun findById(id: RoomId): Room? = repo.findById(id).orElse(null)

    override fun findByHotelId(hotelId: HotelId): List<Room> = repo.findByHotelId(hotelId)

    override fun findAll(): List<Room> = repo.findAll()

    override fun save(room: Room): Room = repo.save(room)
}
