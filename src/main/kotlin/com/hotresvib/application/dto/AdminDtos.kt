package com.hotresvib.application.dto

import java.util.UUID

data class UpdateRoleRequest(
    val role: String  // CUSTOMER, STAFF, ADMIN
)

data class UpdateHotelRequest(
    val name: String? = null,
    val city: String? = null,
    val country: String? = null,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val starRating: Int? = null,
    val isFeatured: Boolean? = null,
    val imageUrl: String? = null
)

data class HotelDetailResponse(
    val id: UUID,
    val name: String,
    val city: String,
    val country: String,
    val description: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val starRating: Int = 0,
    val isFeatured: Boolean = false,
    val imageUrl: String? = null,
    val roomCount: Int = 0
)

data class AdminAnalyticsResponse(
    val totalUsers: Long,
    val totalHotels: Long,
    val totalReservations: Long,
    val confirmedReservations: Long,
    val totalRevenue: Double,
    val recentReservations: List<ReservationResponse>
)
