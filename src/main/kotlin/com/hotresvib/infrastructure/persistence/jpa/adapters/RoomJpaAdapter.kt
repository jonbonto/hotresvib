package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.infrastructure.persistence.jpa.RoomJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
@Primary
class RoomJpaAdapter(private val repo: RoomJpaRepository) : RoomRepository {
    override fun findById(id: RoomId): Room? = repo.findById(id).orElse(null)

    override fun findByHotelId(hotelId: HotelId): List<Room> = repo.findByHotelId(hotelId)

    override fun findAll(): List<Room> = repo.findAll()

    override fun save(room: Room): Room = repo.save(room)
    
    override fun findByHotelIdPaged(hotelId: HotelId, pageable: Pageable): Page<Room> {
        val allRooms = repo.findByHotelId(hotelId)
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, allRooms.size)
        val pageContent = if (start < allRooms.size) allRooms.subList(start, end) else emptyList()
        return org.springframework.data.domain.PageImpl(pageContent, pageable, allRooms.size.toLong())
    }
    
    override fun findByType(type: RoomType, pageable: Pageable): Page<Room> =
        repo.findByType(type, pageable)
    
    override fun findByBaseRateBetween(minRate: BigDecimal, maxRate: BigDecimal, pageable: Pageable): Page<Room> =
        repo.findByBaseRateBetween(minRate, maxRate, pageable)
    
    override fun findByTypeAndBaseRateBetween(
        type: RoomType,
        minRate: BigDecimal,
        maxRate: BigDecimal,
        pageable: Pageable
    ): Page<Room> =
        repo.findByTypeAndBaseRateBetween(type, minRate, maxRate, pageable)
    
    override fun searchByCriteria(
        hotelId: HotelId?,
        type: RoomType?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<Room> =
        repo.searchByCriteria(hotelId, type, minPrice, maxPrice, pageable)
    
    override fun findAllPaged(pageable: Pageable): Page<Room> =
        repo.findAll(pageable)
}
