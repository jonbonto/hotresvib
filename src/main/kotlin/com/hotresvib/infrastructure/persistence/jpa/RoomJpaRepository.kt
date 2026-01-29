package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.HotelId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomJpaRepository : JpaRepository<Room, RoomId> {
    fun findByHotelId(hotelId: HotelId): List<Room>
}
