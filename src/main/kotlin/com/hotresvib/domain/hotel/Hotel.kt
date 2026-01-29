package com.hotresvib.domain.hotel

import com.hotresvib.domain.shared.HotelId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@JvmInline
value class HotelName(val value: String) {
    init {
        require(value.isNotBlank()) { "Hotel name is required" }
    }
}

@Entity
@Table(name = "hotels")
data class Hotel(
    @Id
    val id: HotelId,

    @Column(name = "name", nullable = false)
    val name: HotelName,

    @Column(name = "city", nullable = false)
    val city: String,

    @Column(name = "country", nullable = false)
    val country: String,

    @Column(name = "is_featured", nullable = false)
    val isFeatured: Boolean = false
) {
    init {
        require(city.isNotBlank()) { "City is required" }
        require(country.isNotBlank()) { "Country is required" }
    }
}
