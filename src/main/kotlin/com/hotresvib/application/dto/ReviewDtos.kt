package com.hotresvib.application.dto

import java.util.UUID

data class CreateReviewRequest(
    val hotelId: UUID,
    val reservationId: UUID? = null,
    val rating: Int,
    val comment: String? = null
)

data class UpdateReviewRequest(
    val rating: Int,
    val comment: String? = null
)

data class ReviewResponse(
    val id: UUID,
    val hotelId: UUID,
    val userId: UUID,
    val userName: String,
    val reservationId: UUID? = null,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String,
    val updatedAt: String? = null
)

data class HotelRatingResponse(
    val hotelId: UUID,
    val averageRating: Double,
    val reviewCount: Long
)
