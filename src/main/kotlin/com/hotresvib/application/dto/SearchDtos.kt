package com.hotresvib.application.dto

import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.Money
import java.math.BigDecimal

/**
 * Response DTO for hotel search results
 */
data class HotelSearchResponse(
    val id: String,
    val name: String,
    val city: String,
    val country: String,
    val roomCount: Int,
    val minPrice: BigDecimal?,
    val isFeatured: Boolean
) {
    companion object {
        fun from(hotel: Hotel, roomCount: Int = 0, minPrice: BigDecimal? = null, isFeatured: Boolean = false): HotelSearchResponse {
            return HotelSearchResponse(
                id = hotel.id.value.toString(),
                name = hotel.name.value,
                city = hotel.city,
                country = hotel.country,
                roomCount = roomCount,
                minPrice = minPrice,
                isFeatured = isFeatured
            )
        }
    }
}

/**
 * Response DTO for room search results
 */
data class RoomSearchResponse(
    val id: String,
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val country: String,
    val number: String,
    val type: RoomType,
    val baseRate: BigDecimal,
    val currency: String,
    val isAvailable: Boolean?
) {
    companion object {
        fun from(room: Room, hotel: Hotel, isAvailable: Boolean? = null): RoomSearchResponse {
            return RoomSearchResponse(
                id = room.id.value.toString(),
                hotelId = room.hotelId.value.toString(),
                hotelName = hotel.name.value,
                city = hotel.city,
                country = hotel.country,
                number = room.number.value,
                type = room.type,
                baseRate = room.baseRate.amount,
                currency = room.baseRate.currency,
                isAvailable = isAvailable
            )
        }
    }
}

/**
 * Response DTO for available rooms with calculated pricing
 */
data class RoomAvailabilityResponse(
    val id: String,
    val hotelId: String,
    val hotelName: String,
    val city: String,
    val country: String,
    val number: String,
    val type: RoomType,
    val baseRate: BigDecimal,
    val currency: String,
    val totalPrice: BigDecimal,
    val nights: Int
) {
    companion object {
        fun from(room: Room, hotel: Hotel, totalPrice: Money, nights: Int): RoomAvailabilityResponse {
            return RoomAvailabilityResponse(
                id = room.id.value.toString(),
                hotelId = room.hotelId.value.toString(),
                hotelName = hotel.name.value,
                city = hotel.city,
                country = hotel.country,
                number = room.number.value,
                type = room.type,
                baseRate = room.baseRate.amount,
                currency = room.baseRate.currency,
                totalPrice = totalPrice.amount,
                nights = nights
            )
        }
    }
}

/**
 * Autocomplete suggestion
 */
data class AutocompleteSuggestion(
    val type: String, // "hotel" or "city"
    val value: String,
    val displayText: String
)

/**
 * Price breakdown response
 */
data class PriceBreakdown(
    val basePrice: BigDecimal,
    val currency: String,
    val nights: Int,
    val subtotal: BigDecimal,
    val pricingRulesApplied: List<String>,
    val total: BigDecimal
)
