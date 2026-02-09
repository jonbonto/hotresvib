package com.hotresvib.application.notification

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async

/**
 * Service for listening to and handling email events
 */
@Service
class EmailEventListener(
    private val emailTemplateService: EmailTemplateService,
    private val emailService: EmailService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleBookingConfirmed(event: BookingConfirmedEvent) {
        try {
            logger.info("Processing booking confirmation email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling booking confirmation event", e)
        }
    }

    @EventListener
    @Async
    fun handleCheckInReminder(event: CheckInReminderEvent) {
        try {
            logger.info("Processing check-in reminder email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling check-in reminder event", e)
        }
    }

    @EventListener
    @Async
    fun handleBookingCancelled(event: BookingCancelledEvent) {
        try {
            logger.info("Processing booking cancellation email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling booking cancellation event", e)
        }
    }

    @EventListener
    @Async
    fun handlePaymentFailed(event: PaymentFailedEvent) {
        try {
            logger.info("Processing payment failed email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling payment failed event", e)
        }
    }

    @EventListener
    @Async
    fun handlePaymentSuccessful(event: PaymentSuccessfulEvent) {
        try {
            logger.info("Processing payment successful email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling payment successful event", e)
        }
    }

    @EventListener
    @Async
    fun handlePromotionEmail(event: PromotionEmailEvent) {
        try {
            logger.info("Processing promotion email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling promotion email event", e)
        }
    }

    @EventListener
    @Async
    fun handleReviewRequest(event: ReviewRequestEvent) {
        try {
            logger.info("Processing review request email for: ${event.recipientEmail}")
            val htmlContent = emailTemplateService.renderTemplateWithDefaults(
                event.templateName,
                event.context
            )
            val request = EmailRequest(
                toEmail = event.recipientEmail,
                toName = event.recipientName,
                subject = event.subject,
                htmlContent = htmlContent
            )
            emailService.sendEmail(request)
        } catch (e: Exception) {
            logger.error("Error handling review request event", e)
        }
    }
}
