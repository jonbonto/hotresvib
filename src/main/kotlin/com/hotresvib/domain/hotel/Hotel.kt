package com.hotresvib.domain.hotel

import com.hotresvib.domain.shared.HotelId

@JvmInline
value class HotelName(val value: String) {
    init {
        require(value.isNotBlank()) { "Hotel name is required" }
    }
}

data class Hotel(
    val id: HotelId,
    val name: HotelName,
    val city: String,
    val country: String
) {
    init {
        require(city.isNotBlank()) { "City is required" }
        require(country.isNotBlank()) { "Country is required" }
    }
}
