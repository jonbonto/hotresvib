package com.hotresvib.domain.payment

import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import java.time.Instant

@JvmInline
value class PaymentId(val value: String) {
    init {
        require(value.isNotBlank()) { "Payment id is required" }
    }
}

enum class PaymentStatus {
    INITIATED,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    REFUNDED
}

data class Payment(
    val id: PaymentId,
    val reservationId: ReservationId,
    val amount: Money,
    val status: PaymentStatus,
    val createdAt: Instant
)
