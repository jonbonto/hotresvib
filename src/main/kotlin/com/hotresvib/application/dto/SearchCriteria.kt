package com.hotresvib.application.dto

import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Search criteria for hotels
 */
data class HotelSearchCriteria(
    val city: String? = null,
    val country: String? = null,
    val name: String? = null,
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    val guests: Int? = null
) {
    init {
        if (checkIn != null && checkOut != null) {
            require(checkOut.isAfter(checkIn)) { "Check-out date must be after check-in date" }
        }
        if (guests != null) {
            require(guests > 0) { "Number of guests must be greater than 0" }
        }
    }
}

/**
 * Search criteria for rooms
 */
data class RoomSearchCriteria(
    val hotelId: HotelId? = null,
    val type: RoomType? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val checkIn: LocalDate? = null,
    val checkOut: LocalDate? = null,
    val guests: Int? = null,
    val available: Boolean? = null
) {
    init {
        if (minPrice != null && maxPrice != null) {
            require(maxPrice >= minPrice) { "Maximum price must be greater than or equal to minimum price" }
        }
        if (checkIn != null && checkOut != null) {
            require(checkOut.isAfter(checkIn)) { "Check-out date must be after check-in date" }
        }
        if (guests != null) {
            require(guests > 0) { "Number of guests must be greater than 0" }
        }
    }
}

/**
 * Search criteria for available rooms with date range
 */
data class AvailabilitySearchCriteria(
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val city: String? = null,
    val country: String? = null,
    val type: RoomType? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val guests: Int? = null
) {
    init {
        require(checkOut.isAfter(checkIn)) { "Check-out date must be after check-in date" }
        require(!checkIn.isBefore(LocalDate.now())) { "Check-in date cannot be in the past" }
        if (minPrice != null && maxPrice != null) {
            require(maxPrice >= minPrice) { "Maximum price must be greater than or equal to minimum price" }
        }
        if (guests != null) {
            require(guests > 0) { "Number of guests must be greater than 0" }
        }
    }
}
