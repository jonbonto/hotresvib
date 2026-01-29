package com.hotresvib.application.port

import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface HotelRepository {
    fun findById(id: HotelId): Hotel?
    fun findAll(): List<Hotel>
    fun save(hotel: Hotel): Hotel
    
    // Search methods for Phase 7
    fun findByCityContainingIgnoreCase(city: String, pageable: Pageable): Page<Hotel>
    fun findByCountryContainingIgnoreCase(country: String, pageable: Pageable): Page<Hotel>
    fun searchByCriteria(city: String?, country: String?, name: String?, pageable: Pageable): Page<Hotel>
    fun findByIsFeatured(isFeatured: Boolean, pageable: Pageable): Page<Hotel>
    fun findAllPaged(pageable: Pageable): Page<Hotel>
}
