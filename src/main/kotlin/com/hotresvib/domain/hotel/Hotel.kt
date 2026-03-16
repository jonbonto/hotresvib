package com.hotresvib.domain.hotel

import com.hotresvib.domain.shared.HotelId
import jakarta.persistence.*

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
    val isFeatured: Boolean = false,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "address", length = 500)
    val address: String? = null,

    @Column(name = "phone", length = 50)
    val phone: String? = null,

    @Column(name = "email", length = 255)
    val email: String? = null,

    @Column(name = "star_rating")
    val starRating: Int = 0,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null,
    
    // Phase 11: Optimistic locking
    @Version
    @Column(name = "version")
    val version: Long? = null
) {
    init {
        require(city.isNotBlank()) { "City is required" }
        require(country.isNotBlank()) { "Country is required" }
    }
}
