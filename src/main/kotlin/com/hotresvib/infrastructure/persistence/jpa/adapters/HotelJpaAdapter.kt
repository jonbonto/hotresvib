package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.HotelRepository
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.infrastructure.persistence.jpa.HotelJpaRepository
import org.springframework.stereotype.Repository

@Repository
class HotelJpaAdapter(private val repo: HotelJpaRepository) : HotelRepository {
    override fun findById(id: HotelId): Hotel? = repo.findById(id).orElse(null)

    override fun findAll(): List<Hotel> = repo.findAll()

    override fun save(hotel: Hotel): Hotel = repo.save(hotel)
}
