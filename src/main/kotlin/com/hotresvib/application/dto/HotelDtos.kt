package com.hotresvib.application.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class HotelRequest(
    val name: String,
    val city: String,
    val country: String,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val starRating: Int? = null,
    val isFeatured: Boolean? = null,
    val imageUrl: String? = null
)

data class HotelResponse(
    val id: UUID,
    val name: String,
    val city: String,
    val country: String
)

data class RoomRequest(
    val number: String,
    val type: String,  // SINGLE, DOUBLE, SUITE
    val basePrice: Double,
    val currency: String,
    val description: String? = null,
    val capacity: Int = 2,
    val amenities: String? = null,
    val imageUrl: String? = null
)

data class RoomResponse(
    val id: UUID,
    val hotelId: UUID,
    val number: String,
    val type: String,
    val basePrice: Double,
    val currency: String,
    val description: String? = null,
    val capacity: Int = 2,
    val amenities: String? = null,
    val imageUrl: String? = null
)

data class AvailabilityRequest(
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val quantity: Int
)

data class AvailabilityResponse(
    val id: UUID,
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val quantity: Int
)

data class PricingRuleRequest(
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val description: String? = null
)

data class PricingRuleResponse(
    val id: String,
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val priceAmount: BigDecimal,
    val priceCurrency: String,
    val description: String?
)
