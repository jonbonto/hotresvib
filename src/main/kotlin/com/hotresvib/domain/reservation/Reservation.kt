package com.hotresvib.domain.reservation

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import java.time.Instant
import jakarta.persistence.*

enum class ReservationStatus {
    DRAFT,              // Initial state when reservation created
    PENDING_PAYMENT,    // Payment initiated, waiting for confirmation
    CONFIRMED,          // Payment successful, reservation confirmed
    CANCELLED,          // User cancelled before check-in
    EXPIRED,            // Payment timeout reached
    REFUNDED            // Cancelled reservation with refund processed
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
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "total_amount_amount", nullable = false)),
        AttributeOverride(name = "currency", column = Column(name = "total_amount_currency", nullable = false, length = 3))
    )
    val totalAmount: Money,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: ReservationStatus,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
)
