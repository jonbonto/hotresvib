package com.hotresvib.application.dto

import java.math.BigDecimal
import java.util.UUID

data class PaymentIntentRequest(
    val reservationId: UUID,
    val amount: BigDecimal,
    val currency: String
)

data class PaymentIntentResponse(
    val paymentIntentId: String,
    val clientSecret: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String
)

data class WebhookEventRequest(
    val eventType: String,
    val payload: String
)

data class RefundRequest(
    val reservationId: UUID,
    val reason: String?
)

data class RefundResponse(
    val refundId: String,
    val amount: BigDecimal,
    val status: String,
    val reservationId: UUID
)
