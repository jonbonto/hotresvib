package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.HotelRepository
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import java.util.concurrent.ConcurrentHashMap

class InMemoryHotelRepository : HotelRepository {
    val hotels = ConcurrentHashMap<HotelId, Hotel>()

    override fun findById(id: HotelId): Hotel? = hotels[id]

    override fun findAll(): List<Hotel> = hotels.values.toList()

    override fun save(hotel: Hotel): Hotel {
        hotels[hotel.id] = hotel
        return hotel
    }
}
