package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.shared.ReservationId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PaymentJpaRepository : JpaRepository<Payment, UUID> {
    fun findByReservationId(reservationId: ReservationId): List<Payment>
    fun findByPaymentIntentId(paymentIntentId: String): Payment?
    fun findByIdempotencyKey(idempotencyKey: String): Payment?
}
