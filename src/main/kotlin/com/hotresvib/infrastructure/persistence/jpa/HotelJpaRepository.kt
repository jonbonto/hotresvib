package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.hotel.Hotel
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface HotelJpaRepository : JpaRepository<Hotel, UUID> {
    fun findByCityContainingIgnoreCase(city: String, pageable: Pageable): Page<Hotel>
    fun findByCountryContainingIgnoreCase(country: String, pageable: Pageable): Page<Hotel>
    fun findByIsFeatured(isFeatured: Boolean, pageable: Pageable): Page<Hotel>
    
    @Query("""
        SELECT h FROM Hotel h 
        WHERE (:city IS NULL OR LOWER(h.city) LIKE LOWER(CONCAT('%', :city, '%')))
        AND (:country IS NULL OR LOWER(h.country) LIKE LOWER(CONCAT('%', :country, '%')))
        AND (:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%')))
    """)
    fun searchByCriteria(
        @Param("city") city: String?,
        @Param("country") country: String?,
        @Param("name") name: String?,
        pageable: Pageable
    ): Page<Hotel>
}
