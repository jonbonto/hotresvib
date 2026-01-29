package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.HotelRepository
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.infrastructure.persistence.jpa.HotelJpaRepository
import java.util.UUID
import org.springframework.context.annotation.Primary
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
@Primary
class HotelJpaAdapter(private val repo: HotelJpaRepository) : HotelRepository {
    override fun findById(id: HotelId): Hotel? = repo.findById(id.value).orElse(null)

    override fun findAll(): List<Hotel> = repo.findAll()

    override fun save(hotel: Hotel): Hotel = repo.save(hotel)
    
    override fun findByCityContainingIgnoreCase(city: String, pageable: Pageable): Page<Hotel> =
        repo.findByCityContainingIgnoreCase(city, pageable)
    
    override fun findByCountryContainingIgnoreCase(country: String, pageable: Pageable): Page<Hotel> =
        repo.findByCountryContainingIgnoreCase(country, pageable)
    
    override fun searchByCriteria(city: String?, country: String?, name: String?, pageable: Pageable): Page<Hotel> =
        repo.searchByCriteria(city, country, name, pageable)
    
    override fun findByIsFeatured(isFeatured: Boolean, pageable: Pageable): Page<Hotel> =
        repo.findByIsFeatured(isFeatured, pageable)
    
    override fun findAllPaged(pageable: Pageable): Page<Hotel> =
        repo.findAll(pageable)
}
