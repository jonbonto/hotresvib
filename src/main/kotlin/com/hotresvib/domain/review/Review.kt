package com.hotresvib.domain.review

import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "hotel_id", nullable = false)
    val hotelId: HotelId,

    @Column(name = "user_id", nullable = false)
    val userId: UserId,

    @Column(name = "reservation_id")
    val reservationId: ReservationId? = null,

    @Column(name = "rating", nullable = false)
    val rating: Int,

    @Column(name = "comment", columnDefinition = "TEXT")
    val comment: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    val updatedAt: Instant? = null
) {
    init {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
    }
}
