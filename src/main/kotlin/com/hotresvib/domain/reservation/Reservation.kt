package com.hotresvib.domain.reservation

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import java.time.Instant

enum class ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}

data class Reservation(
    val id: ReservationId,
    val userId: UserId,
    val roomId: RoomId,
    val stay: DateRange,
    val totalAmount: Money,
    val status: ReservationStatus,
    val createdAt: Instant
)
