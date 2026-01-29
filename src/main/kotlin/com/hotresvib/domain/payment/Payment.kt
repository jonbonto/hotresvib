package com.hotresvib.domain.payment

import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import java.time.Instant
import java.util.UUID
import jakarta.persistence.*

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    val id: UUID,

    val reservationId: ReservationId,

    @Embedded
    val amount: Money,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    val status: PaymentStatus,

    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String,

    @Column(name = "transaction_id")
    val transactionId: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant
)
