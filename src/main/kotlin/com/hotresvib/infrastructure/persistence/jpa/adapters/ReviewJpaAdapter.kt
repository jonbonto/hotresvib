package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.ReviewRepository
import com.hotresvib.domain.review.Review
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.persistence.jpa.ReviewJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@Primary
class ReviewJpaAdapter(private val repo: ReviewJpaRepository) : ReviewRepository {
    override fun findById(id: UUID): Review? = repo.findById(id).orElse(null)
    override fun findByHotelId(hotelId: HotelId): List<Review> = repo.findByHotelId(hotelId)
    override fun findByUserId(userId: UserId): List<Review> = repo.findByUserId(userId)
    override fun findByHotelIdAndUserId(hotelId: HotelId, userId: UserId): Review? = repo.findByHotelIdAndUserId(hotelId, userId)
    override fun save(review: Review): Review = repo.save(review)
    override fun deleteById(id: UUID) = repo.deleteById(id)
    override fun averageRatingByHotelId(hotelId: HotelId): Double? = repo.averageRatingByHotelId(hotelId)
    override fun countByHotelId(hotelId: HotelId): Long = repo.countByHotelId(hotelId)
}
