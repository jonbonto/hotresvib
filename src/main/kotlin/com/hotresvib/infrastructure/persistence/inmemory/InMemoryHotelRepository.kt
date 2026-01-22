package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.HotelRepository
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import java.util.concurrent.ConcurrentHashMap

class InMemoryHotelRepository : HotelRepository {
    private val storage = ConcurrentHashMap<HotelId, Hotel>()

    override fun findById(id: HotelId): Hotel? = storage[id]

    override fun save(hotel: Hotel): Hotel {
        storage[hotel.id] = hotel
        return hotel
    }
}
