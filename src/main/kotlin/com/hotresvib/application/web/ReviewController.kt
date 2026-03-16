package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.port.HotelRepository
import com.hotresvib.application.port.ReviewRepository
import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.review.Review
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = ["*"])
class ReviewController(
    private val reviewRepository: ReviewRepository,
    private val hotelRepository: HotelRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/hotel/{hotelId}")
    fun getHotelReviews(@PathVariable hotelId: UUID): ResponseEntity<List<ReviewResponse>> {
        val reviews = reviewRepository.findByHotelId(HotelId(hotelId))
        val responses = reviews.map { review ->
            val user = userRepository.findById(review.userId)
            ReviewResponse(
                id = review.id,
                hotelId = review.hotelId.value,
                userId = review.userId.value,
                userName = user?.displayName ?: "Unknown",
                reservationId = review.reservationId?.value,
                rating = review.rating,
                comment = review.comment,
                createdAt = review.createdAt.toString(),
                updatedAt = review.updatedAt?.toString()
            )
        }
        return ResponseEntity.ok(responses)
    }

    @GetMapping("/hotel/{hotelId}/rating")
    fun getHotelRating(@PathVariable hotelId: UUID): ResponseEntity<HotelRatingResponse> {
        val avgRating = reviewRepository.averageRatingByHotelId(HotelId(hotelId)) ?: 0.0
        val count = reviewRepository.countByHotelId(HotelId(hotelId))
        return ResponseEntity.ok(HotelRatingResponse(
            hotelId = hotelId,
            averageRating = avgRating,
            reviewCount = count
        ))
    }

    @PostMapping
    fun createReview(
        @RequestBody request: CreateReviewRequest,
        authentication: Authentication?
    ): ResponseEntity<ReviewResponse> {
        val auth = authentication ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        val userId = try {
            UserId(UUID.fromString(auth.name))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        }

        // Check hotel exists
        hotelRepository.findById(HotelId(request.hotelId))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found")

        // Check if user already reviewed this hotel
        val existing = reviewRepository.findByHotelIdAndUserId(HotelId(request.hotelId), userId)
        if (existing != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "You have already reviewed this hotel")
        }

        val user = userRepository.findById(userId)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")

        val review = Review(
            hotelId = HotelId(request.hotelId),
            userId = userId,
            reservationId = request.reservationId?.let { ReservationId(it) },
            rating = request.rating,
            comment = request.comment
        )
        val saved = reviewRepository.save(review)

        return ResponseEntity.status(HttpStatus.CREATED).body(ReviewResponse(
            id = saved.id,
            hotelId = saved.hotelId.value,
            userId = saved.userId.value,
            userName = user.displayName,
            reservationId = saved.reservationId?.value,
            rating = saved.rating,
            comment = saved.comment,
            createdAt = saved.createdAt.toString(),
            updatedAt = saved.updatedAt?.toString()
        ))
    }

    @PutMapping("/{id}")
    fun updateReview(
        @PathVariable id: UUID,
        @RequestBody request: UpdateReviewRequest,
        authentication: Authentication?
    ): ResponseEntity<ReviewResponse> {
        val auth = authentication ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        val userId = try {
            UserId(UUID.fromString(auth.name))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        }

        val review = reviewRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found")

        if (review.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to update this review")
        }

        val user = userRepository.findById(userId)

        val updated = review.copy(
            rating = request.rating,
            comment = request.comment,
            updatedAt = Instant.now()
        )
        val saved = reviewRepository.save(updated)

        return ResponseEntity.ok(ReviewResponse(
            id = saved.id,
            hotelId = saved.hotelId.value,
            userId = saved.userId.value,
            userName = user?.displayName ?: "Unknown",
            reservationId = saved.reservationId?.value,
            rating = saved.rating,
            comment = saved.comment,
            createdAt = saved.createdAt.toString(),
            updatedAt = saved.updatedAt?.toString()
        ))
    }

    @DeleteMapping("/{id}")
    fun deleteReview(
        @PathVariable id: UUID,
        authentication: Authentication?
    ): ResponseEntity<Void> {
        val auth = authentication ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        val userId = try {
            UserId(UUID.fromString(auth.name))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required")
        }

        val review = reviewRepository.findById(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found")

        if (review.userId != userId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to delete this review")
        }

        reviewRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
