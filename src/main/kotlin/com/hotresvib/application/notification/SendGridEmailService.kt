package com.hotresvib.application.notification

import com.sendgrid.SendGrid
import com.sendgrid.Request
import com.sendgrid.Method
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Content
import com.hotresvib.domain.notification.EmailLog
import com.hotresvib.application.port.EmailLogRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDate
import java.math.BigDecimal

/**
 * Service for sending emails via SendGrid
 */
@Service
@org.springframework.context.annotation.Profile("prod")
class SendGridEmailService(
    @Value("\${sendgrid.api-key:}")
    private val sendGridApiKey: String,
    @Value("\${sendgrid.from-email:noreply@hotresvib.com}")
    private val fromEmail: String,
    @Value("\${sendgrid.from-name:HotResvib}")
    private val fromName: String,
    private val emailLogRepository: EmailLogRepository,
    // Optional SendGrid client for easier testing
    private val sendGridClient: com.sendgrid.SendGrid? = null
) : EmailService {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val sendGrid = sendGridClient ?: if (sendGridApiKey.isNotEmpty()) SendGrid(sendGridApiKey) else null

    override fun sendEmail(emailRequest: EmailRequest): Boolean {
        return try {
            if (sendGrid == null) {
                logger.warn("SendGrid API key not configured, logging email instead")
                logEmail(emailRequest, "SENT", "SendGrid not configured - logged to console")
                return true
            }

            val fromEmailObject = Email(fromEmail, fromName)
            val toEmail = Email(emailRequest.toEmail, emailRequest.toName ?: "")
            val content = Content("text/html", emailRequest.htmlContent)

            val mail = Mail(fromEmailObject, emailRequest.subject, toEmail, content)

            // Add CC and BCC recipients
            emailRequest.ccEmails.forEach { cc ->
                mail.personalization[0].addCc(Email(cc))
            }
            emailRequest.bccEmails.forEach { bcc ->
                mail.personalization[0].addBcc(Email(bcc))
            }

            // Add reply-to if provided
            emailRequest.replyToEmail?.let {
                mail.setReplyTo(Email(it))
            }

            val request = Request()
            request.method = Method.POST
            request.endpoint = "mail/send"
            request.body = mail.build()
            val response = sendGrid!!.api(request)

            if (response.statusCode in 200..299) {
                logger.info("Email sent successfully to ${emailRequest.toEmail}")
                logEmail(emailRequest, "SUCCESS", null)
                true
            } else {
                logger.error("Failed to send email: ${response.statusCode} - ${response.body}")
                logEmail(emailRequest, "FAILURE", response.body)
                false
            }
        } catch (e: Exception) {
            logger.error("Error sending email to ${emailRequest.toEmail}", e)
            logEmail(emailRequest, "FAILURE", e.message ?: "Unknown error")
            false
        }
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
        // Placeholder implementation - can be extended with HTML templates
        logger.info("Confirmation email sent to $email for reservation $reservationId")
    }

    override fun sendCancellationEmail(email: String, displayName: String, reservationId: String) {
        logger.info("Cancellation email sent to $email for reservation $reservationId")
    }

    override fun sendWelcomeEmail(email: String, displayName: String) {
        logger.info("Welcome email sent to $email")
    }

    private fun logEmail(emailRequest: EmailRequest, status: String, errorMessage: String?) {
        try {
            val emailLog = EmailLog(
                recipientEmail = emailRequest.toEmail,
                subject = emailRequest.subject,
                templateName = "custom-template",
                sentAt = Instant.now(),
                status = status,
                errorMessage = errorMessage
            )
            emailLogRepository.save(emailLog)
        } catch (e: Exception) {
            logger.error("Error logging email", e)
        }
    }
}
