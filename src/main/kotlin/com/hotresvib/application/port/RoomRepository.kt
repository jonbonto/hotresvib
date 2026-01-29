package com.hotresvib.application.port

import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

interface RoomRepository {
    fun findById(id: RoomId): Room?
    fun findByHotelId(hotelId: HotelId): List<Room>
    fun findAll(): List<Room>
    fun save(room: Room): Room
    
    // Search methods for Phase 7
    fun findByHotelIdPaged(hotelId: HotelId, pageable: Pageable): Page<Room>
    fun findByType(type: RoomType, pageable: Pageable): Page<Room>
    fun findByBaseRateBetween(minRate: BigDecimal, maxRate: BigDecimal, pageable: Pageable): Page<Room>
    fun findByTypeAndBaseRateBetween(type: RoomType, minRate: BigDecimal, maxRate: BigDecimal, pageable: Pageable): Page<Room>
    fun searchByCriteria(
        hotelId: HotelId?,
        type: RoomType?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<Room>
    fun findAllPaged(pageable: Pageable): Page<Room>
}
