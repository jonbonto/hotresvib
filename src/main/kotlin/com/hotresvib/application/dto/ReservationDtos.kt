package com.hotresvib.application.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.Instant
import java.util.UUID

data class ReservationRequest(
    val roomId: UUID,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate
)

data class ReservationResponse(
    val id: UUID,
    val userId: UUID,
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalPrice: Double,
    val status: String,  // PENDING, CONFIRMED, CANCELLED
    val createdAt: String
)

data class CheckAvailabilityRequest(
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class CheckAvailabilityResponse(
    val available: Boolean,
    val roomId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalPrice: Double = 0.0
)

data class PaymentRequest(
    val reservationId: UUID,
    val amount: Double,
    val currency: String,
    val paymentMethod: String  // CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER
)

data class PaymentResponse(
    val id: UUID,
    val reservationId: UUID,
    val amount: Double,
    val currency: String,
    val status: String,  // PENDING, COMPLETED, FAILED
    val paymentMethod: String,
    val transactionId: String?
)
