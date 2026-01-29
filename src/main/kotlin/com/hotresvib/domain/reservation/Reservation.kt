package com.hotresvib.domain.reservation

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import java.time.Instant
import jakarta.persistence.*

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

@Entity
@Table(name = "reservations")
data class Reservation(
    @Id
    val id: ReservationId,

    val userId: UserId,

    val roomId: RoomId,

    @Embedded
    val stay: DateRange,

    @Embedded
    val totalAmount: Money,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ReservationStatus,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
)
