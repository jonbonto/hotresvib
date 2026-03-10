package com.hotresvib.application.notification

import org.springframework.stereotype.Service
import java.time.LocalDate
import java.math.BigDecimal

/**
 * Data class for email requests
 */
data class EmailRequest(
    val toEmail: String,
    val toName: String? = null,
    val subject: String,
    val htmlContent: String,
    val ccEmails: List<String> = emptyList(),
    val bccEmails: List<String> = emptyList(),
    val replyToEmail: String? = null
)

interface EmailService {
    /**
     * Send a single email
     * @return true if email was sent successfully
     */
    fun sendEmail(emailRequest: EmailRequest): Boolean

    /**
     * Send multiple emails in batch
     * @return map of email to success status
     */
    fun sendBatchEmails(requests: List<EmailRequest>): Map<String, Boolean>

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
@org.springframework.context.annotation.Profile("!prod") // active in dev/test (everything except prod)
class EmailServiceImpl : EmailService {
    
    override fun sendEmail(emailRequest: EmailRequest): Boolean {
        logEmail(emailRequest)
        return true
    }

    override fun sendBatchEmails(requests: List<EmailRequest>): Map<String, Boolean> {
        return requests.associate { it.toEmail to sendEmail(it) }
    }
    
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
        
        logSimpleEmail(email, subject, body)
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
        
        logSimpleEmail(email, subject, body)
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
        
        logSimpleEmail(email, subject, body)
    }

    private fun logEmail(emailRequest: EmailRequest) {
        println("""
            ========== EMAIL LOG ==========
            To: ${emailRequest.toEmail}
            Subject: ${emailRequest.subject}
            
            ${emailRequest.htmlContent}
            ================================
        """.trimIndent())
    }

    private fun logSimpleEmail(email: String, subject: String, body: String) {
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
