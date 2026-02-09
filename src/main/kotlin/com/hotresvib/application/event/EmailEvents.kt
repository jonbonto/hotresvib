package com.hotresvib.application.event

import org.springframework.context.ApplicationEvent

/**
 * Base email event for Spring ApplicationEventPublisher
 */
sealed class EmailEvent(
    source: Any,
    val recipientEmail: String,
    val subject: String,
    val templateName: String,
    val context: Map<String, Any>
) : ApplicationEvent(source)

/**
 * Event fired when a booking is confirmed
 */
class BookingConfirmedEvent(
    source: Any,
    recipientEmail: String,
    context: Map<String, Any>
) : EmailEvent(
    source,
    recipientEmail,
    "Booking Confirmation - ${context["hotelName"]}",
    "email/booking-confirmation",
    context
)

/**
 * Event fired when a booking is cancelled
 */
class BookingCancelledEvent(
    source: Any,
    recipientEmail: String,
    context: Map<String, Any>
) : EmailEvent(
    source,
    recipientEmail,
    "Booking Cancelled - Refund Information",
    "email/cancellation-notification",
    context
)

/**
 * Event fired when payment is received
 */
class PaymentReceivedEvent(
    source: Any,
    recipientEmail: String,
    context: Map<String, Any>
) : EmailEvent(
    source,
    recipientEmail,
    "Payment Receipt - Invoice #${context["invoiceNumber"]}",
    "email/payment-receipt",
    context
)

/**
 * Event fired 24 hours before check-in
 */
class CheckInReminderEvent(
    source: Any,
    recipientEmail: String,
    context: Map<String, Any>
) : EmailEvent(
    source,
    recipientEmail,
    "Check-in Reminder - ${context["hotelName"]}",
    "email/check-in-reminder",
    context
)

/**
 * Event fired for password reset
 */
class PasswordResetEvent(
    source: Any,
    recipientEmail: String,
    context: Map<String, Any>
) : EmailEvent(
    source,
    recipientEmail,
    "Password Reset Request",
    "email/password-reset",
    context
)
