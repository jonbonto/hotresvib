package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.HotelId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface RoomJpaRepository : JpaRepository<Room, RoomId> {
    fun findByHotelId(hotelId: HotelId): List<Room>
    fun findByType(type: RoomType, pageable: Pageable): Page<Room>
    
    @Query("SELECT r FROM Room r WHERE r.baseRate.amount BETWEEN :minRate AND :maxRate")
    fun findByBaseRateBetween(
        @Param("minRate") minRate: BigDecimal,
        @Param("maxRate") maxRate: BigDecimal,
        pageable: Pageable
    ): Page<Room>
    
    @Query("SELECT r FROM Room r WHERE r.type = :type AND r.baseRate.amount BETWEEN :minRate AND :maxRate")
    fun findByTypeAndBaseRateBetween(
        @Param("type") type: RoomType,
        @Param("minRate") minRate: BigDecimal,
        @Param("maxRate") maxRate: BigDecimal,
        pageable: Pageable
    ): Page<Room>
    
    @Query("""
        SELECT r FROM Room r 
        WHERE (:hotelId IS NULL OR r.hotelId = :hotelId)
        AND (:type IS NULL OR r.type = :type)
        AND (:minPrice IS NULL OR r.baseRate.amount >= :minPrice)
        AND (:maxPrice IS NULL OR r.baseRate.amount <= :maxPrice)
    """)
    fun searchByCriteria(
        @Param("hotelId") hotelId: HotelId?,
        @Param("type") type: RoomType?,
        @Param("minPrice") minPrice: BigDecimal?,
        @Param("maxPrice") maxPrice: BigDecimal?,
        pageable: Pageable
    ): Page<Room>
}
