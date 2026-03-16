package com.hotresvib.application.port

import com.hotresvib.domain.review.Review
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.UserId
import java.util.UUID

interface ReviewRepository {
    fun findById(id: UUID): Review?
    fun findByHotelId(hotelId: HotelId): List<Review>
    fun findByUserId(userId: UserId): List<Review>
    fun findByHotelIdAndUserId(hotelId: HotelId, userId: UserId): Review?
    fun save(review: Review): Review
    fun deleteById(id: UUID)
    fun averageRatingByHotelId(hotelId: HotelId): Double?
    fun countByHotelId(hotelId: HotelId): Long
}
