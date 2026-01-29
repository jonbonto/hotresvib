package com.hotresvib.application.port

import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId

interface HotelRepository {
    fun findById(id: HotelId): Hotel?
    fun findAll(): List<Hotel>
    fun save(hotel: Hotel): Hotel
}
