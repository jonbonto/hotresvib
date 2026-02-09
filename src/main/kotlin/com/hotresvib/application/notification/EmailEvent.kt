package com.hotresvib.application.notification

import org.springframework.context.ApplicationEvent
import java.time.LocalDateTime

/**
 * Base class for email events
 */
abstract class EmailEvent(
    source: Any,
    val templateName: String,
    val recipientEmail: String,
    val recipientName: String? = null,
    val subject: String,
    val context: Map<String, Any>
) : ApplicationEvent(source)

/**
 * Event emitted when a booking is confirmed
 */
data class BookingConfirmedEvent(
    val source2: Any,
    val bookingId: Long,
    val hotelName: String,
    val checkInDate: String,
    val checkOutDate: String,
    val email: String,
    val guestName: String,
    val confirmationNumber: String,
    val totalPrice: Double
) : EmailEvent(
    source = source2,
    templateName = "email/booking-confirmation",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Booking Confirmation - HotResvib",
    context = mapOf(
        "guestName" to guestName,
        "hotelName" to hotelName,
        "checkInDate" to checkInDate,
        "checkOutDate" to checkOutDate,
        "confirmationNumber" to confirmationNumber,
        "totalPrice" to totalPrice,
        "bookingId" to bookingId
    )
)

/**
 * Event emitted before check-in reminder
 */
data class CheckInReminderEvent(
    val source2: Any,
    val bookingId: Long,
    val hotelName: String,
    val checkInDate: String,
    val email: String,
    val guestName: String,
    val hotelAddress: String,
    val checkInTime: String
) : EmailEvent(
    source = source2,
    templateName = "email/check-in-reminder",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Check-In Reminder - $hotelName",
    context = mapOf(
        "guestName" to guestName,
        "hotelName" to hotelName,
        "checkInDate" to checkInDate,
        "hotelAddress" to hotelAddress,
        "checkInTime" to checkInTime,
        "bookingId" to bookingId
    )
)

/**
 * Event emitted when booking is cancelled
 */
data class BookingCancelledEvent(
    val source2: Any,
    val bookingId: Long,
    val hotelName: String,
    val email: String,
    val guestName: String,
    val refundAmount: Double,
    val cancelReason: String? = null
) : EmailEvent(
    source = source2,
    templateName = "email/booking-cancelled",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Booking Cancellation - HotResvib",
    context = mapOf(
        "guestName" to guestName,
        "hotelName" to hotelName,
        "bookingId" to bookingId,
        "refundAmount" to refundAmount,
        "cancelReason" to (cancelReason ?: "Cancelled by guest")
    )
)

/**
 * Event emitted for payment failures
 */
data class PaymentFailedEvent(
    val source2: Any,
    val bookingId: Long,
    val email: String,
    val guestName: String,
    val reason: String,
    val retryTime: String? = null
) : EmailEvent(
    source = source2,
    templateName = "email/payment-failed",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Payment Failed - Action Required",
    context = mapOf(
        "guestName" to guestName,
        "bookingId" to bookingId,
        "reason" to reason,
        "retryTime" to (retryTime ?: "within 24 hours")
    )
)

/**
 * Event emitted when payment is successful
 */
data class PaymentSuccessfulEvent(
    val source2: Any,
    val bookingId: Long,
    val email: String,
    val guestName: String,
    val amount: Double,
    val transactionId: String
) : EmailEvent(
    source = source2,
    templateName = "email/payment-successful",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Payment Confirmed - HotResvib",
    context = mapOf(
        "guestName" to guestName,
        "bookingId" to bookingId,
        "amount" to amount,
        "transactionId" to transactionId
    )
)

/**
 * Event emitted for special promotions or offers
 */
data class PromotionEmailEvent(
    val source2: Any,
    val email: String,
    val guestName: String,
    val promotionTitle: String,
    val promotionDescription: String,
    val discountCode: String,
    val expiryDate: String
) : EmailEvent(
    source = source2,
    templateName = "email/promotion",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Exclusive Offer - $promotionTitle",
    context = mapOf(
        "guestName" to guestName,
        "promotionTitle" to promotionTitle,
        "promotionDescription" to promotionDescription,
        "discountCode" to discountCode,
        "expiryDate" to expiryDate
    )
)

/**
 * Event emitted for review request
 */
data class ReviewRequestEvent(
    val source2: Any,
    val bookingId: Long,
    val hotelName: String,
    val email: String,
    val guestName: String,
    val checkOutDate: String
) : EmailEvent(
    source = source2,
    templateName = "email/review-request",
    recipientEmail = email,
    recipientName = guestName,
    subject = "Share Your Experience - $hotelName",
    context = mapOf(
        "guestName" to guestName,
        "hotelName" to hotelName,
        "bookingId" to bookingId,
        "checkOutDate" to checkOutDate
    )
)
