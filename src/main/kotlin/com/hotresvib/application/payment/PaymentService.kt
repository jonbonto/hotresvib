package com.hotresvib.application.payment

import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.Money
import java.math.BigDecimal
import org.springframework.stereotype.Service

interface PaymentService {
    fun processPayment(
        reservationId: ReservationId,
        amount: Money,
        paymentMethod: String
    ): Payment

    fun refundPayment(paymentId: String): Payment

    fun getPaymentStatus(paymentId: String): PaymentStatus
}

@Service
class PaymentServiceImpl : PaymentService {
    
    override fun processPayment(
        reservationId: ReservationId,
        amount: Money,
        paymentMethod: String
    ): Payment {
        // In production, integrate with payment gateway (Stripe, PayPal, etc.)
        // For now, simulate successful payment
        
        logPaymentAttempt(reservationId.value.toString(), amount, paymentMethod)
        
        return Payment(
            id = java.util.UUID.randomUUID(),
            reservationId = reservationId,
            amount = amount,
            status = PaymentStatus.COMPLETED,
            paymentMethod = paymentMethod,
            transactionId = "TXN_" + java.util.UUID.randomUUID().toString().substring(0, 8).uppercase(),
            paymentIntentId = null,
            metadata = null,
            idempotencyKey = null,
            createdAt = java.time.Instant.now()
        )
    }

    override fun refundPayment(paymentId: String): Payment {
        // Simulate refund
        logRefundAttempt(paymentId)
        
        return Payment(
            id = java.util.UUID.fromString(paymentId),
            reservationId = ReservationId(java.util.UUID.randomUUID()),
            amount = Money(BigDecimal("0"), "USD"),
            status = PaymentStatus.REFUNDED,
            paymentMethod = "REFUND",
            transactionId = null,
            paymentIntentId = null,
            metadata = null,
            idempotencyKey = null,
            createdAt = java.time.Instant.now()
        )
    }

    override fun getPaymentStatus(paymentId: String): PaymentStatus {
        return PaymentStatus.COMPLETED
    }

    private fun logPaymentAttempt(reservationId: String, amount: Money, method: String) {
        println("""
            ========== PAYMENT LOG ==========
            Reservation ID: $reservationId
            Amount: ${amount.amount} ${amount.currency}
            Method: $method
            Status: SIMULATED PROCESSING
            ================================
        """.trimIndent())
    }

    private fun logRefundAttempt(paymentId: String) {
        println("""
            ========== REFUND LOG ==========
            Payment ID: $paymentId
            Status: REFUNDED
            ================================
        """.trimIndent())
    }
}
