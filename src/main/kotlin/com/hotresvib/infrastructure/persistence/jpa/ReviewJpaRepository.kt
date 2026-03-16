package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.review.Review
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReviewJpaRepository : JpaRepository<Review, UUID> {
    fun findByHotelId(hotelId: HotelId): List<Review>
    fun findByUserId(userId: UserId): List<Review>
    fun findByHotelIdAndUserId(hotelId: HotelId, userId: UserId): Review?

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotelId = :hotelId")
    fun averageRatingByHotelId(hotelId: HotelId): Double?

    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotelId = :hotelId")
    fun countByHotelId(hotelId: HotelId): Long
}
