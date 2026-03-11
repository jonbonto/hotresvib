package com.hotresvib.application.payment

import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.Money
import com.hotresvib.application.port.ReservationRepository
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
class PaymentServiceImpl(
    private val reservationRepository: ReservationRepository
) : PaymentService {
    
    override fun processPayment(
        reservationId: ReservationId,
        amount: Money,
        paymentMethod: String
    ): Payment {
        // In production, integrate with payment gateway (Stripe, PayPal, etc.)
        // For now, simulate successful payment
        
        logPaymentAttempt(reservationId.value.toString(), amount, paymentMethod)
        
        // Update reservation status to PENDING_PAYMENT (payment in progress)
        val reservation = reservationRepository.findById(reservationId)
        if (reservation != null) {
            val pendingReservation = reservation.copy(status = ReservationStatus.PENDING_PAYMENT)
            reservationRepository.save(pendingReservation)
            logReservationPending(reservationId.value.toString())
        }
        
        return Payment(
            id = java.util.UUID.randomUUID(),
            reservationId = reservationId,
            amount = amount,
            status = PaymentStatus.PENDING,
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

    private fun logReservationPending(reservationId: String) {
        println("""
            ========== RESERVATION PENDING ==========
            Reservation ID: $reservationId
            Status: PENDING_PAYMENT (Payment initiated)
            =========================================
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
