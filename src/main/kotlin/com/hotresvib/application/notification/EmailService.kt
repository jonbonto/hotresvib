package com.hotresvib.application.notification

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.math.BigDecimal

interface EmailService {
    fun sendConfirmationEmail(
        email: String,
        displayName: String,
        reservationId: String,
        hotelName: String,
        roomNumber: String,
        checkInDate: LocalDate,
        checkOutDate: LocalDate,
        totalAmount: BigDecimal,
        currency: String
    )

    fun sendCancellationEmail(
        email: String,
        displayName: String,
        reservationId: String
    )

    fun sendWelcomeEmail(
        email: String,
        displayName: String
    )
}

@Service
class EmailServiceImpl : EmailService {
    
    override fun sendConfirmationEmail(
        email: String,
        displayName: String,
        reservationId: String,
        hotelName: String,
        roomNumber: String,
        checkInDate: LocalDate,
        checkOutDate: LocalDate,
        totalAmount: BigDecimal,
        currency: String
    ) {
        // In production, integrate with email provider (SendGrid, AWS SES, etc.)
        val subject = "Reservation Confirmed - HotResvib"
        val body = """
            Dear $displayName,
            
            Your reservation has been confirmed!
            
            Reservation ID: $reservationId
            Hotel: $hotelName
            Room: $roomNumber
            Check-in: $checkInDate
            Check-out: $checkOutDate
            Total Amount: $totalAmount $currency
            
            Thank you for booking with HotResvib!
        """.trimIndent()
        
        logEmail(email, subject, body)
    }

    override fun sendCancellationEmail(
        email: String,
        displayName: String,
        reservationId: String
    ) {
        val subject = "Reservation Cancelled - HotResvib"
        val body = """
            Dear $displayName,
            
            Your reservation has been cancelled.
            
            Reservation ID: $reservationId
            
            If you have questions, please contact our support team.
        """.trimIndent()
        
        logEmail(email, subject, body)
    }

    override fun sendWelcomeEmail(
        email: String,
        displayName: String
    ) {
        val subject = "Welcome to HotResvib!"
        val body = """
            Dear $displayName,
            
            Welcome to HotResvib! We're excited to have you as a member.
            
            You can now browse and book hotels with us. Happy travels!
        """.trimIndent()
        
        logEmail(email, subject, body)
    }

    private fun logEmail(email: String, subject: String, body: String) {
        // Log email for now (in production, send via email provider)
        println("""
            ========== EMAIL LOG ==========
            To: $email
            Subject: $subject
            
            $body
            ================================
        """.trimIndent())
    }
}
