package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
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
    
    override fun findByHotelIdPaged(hotelId: HotelId, pageable: Pageable): Page<Room> {
        val filtered = synchronized(lock) {
            rooms.values.filter { it.hotelId == hotelId }
        }
        return paginateList(filtered, pageable)
    }
    
    override fun findByType(type: RoomType, pageable: Pageable): Page<Room> {
        val filtered = synchronized(lock) {
            rooms.values.filter { it.type == type }
        }
        return paginateList(filtered, pageable)
    }
    
    override fun findByBaseRateBetween(minRate: BigDecimal, maxRate: BigDecimal, pageable: Pageable): Page<Room> {
        val filtered = synchronized(lock) {
            rooms.values.filter { it.baseRate.amount >= minRate && it.baseRate.amount <= maxRate }
        }
        return paginateList(filtered, pageable)
    }
    
    override fun findByTypeAndBaseRateBetween(
        type: RoomType,
        minRate: BigDecimal,
        maxRate: BigDecimal,
        pageable: Pageable
    ): Page<Room> {
        val filtered = synchronized(lock) {
            rooms.values.filter {
                it.type == type &&
                it.baseRate.amount >= minRate &&
                it.baseRate.amount <= maxRate
            }
        }
        return paginateList(filtered, pageable)
    }
    
    override fun searchByCriteria(
        hotelId: HotelId?,
        type: RoomType?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<Room> {
        var filtered = synchronized(lock) {
            rooms.values.toList()
        }
        
        if (hotelId != null) {
            filtered = filtered.filter { it.hotelId == hotelId }
        }
        if (type != null) {
            filtered = filtered.filter { it.type == type }
        }
        if (minPrice != null) {
            filtered = filtered.filter { it.baseRate.amount >= minPrice }
        }
        if (maxPrice != null) {
            filtered = filtered.filter { it.baseRate.amount <= maxPrice }
        }
        
        return paginateList(filtered, pageable)
    }
    
    override fun findAllPaged(pageable: Pageable): Page<Room> {
        val allRooms = synchronized(lock) {
            rooms.values.toList()
        }
        return paginateList(allRooms, pageable)
    }
    
    private fun paginateList(list: List<Room>, pageable: Pageable): Page<Room> {
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, list.size)
        val pageContent = if (start < list.size) list.subList(start, end) else emptyList()
        return PageImpl(pageContent, pageable, list.size.toLong())
    }
}
