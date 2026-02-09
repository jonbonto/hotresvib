package com.hotresvib.application.notification

import com.hotresvib.application.port.EmailUnsubscribeRepository
import com.hotresvib.domain.notification.EmailUnsubscribe
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory

/**
 * Service for managing email subscriptions and preferences
 */
@Service
class EmailPreferenceService(
    private val unsubscribeRepository: EmailUnsubscribeRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Check if an email address is unsubscribed from specific email type
     */
    fun isUnsubscribed(email: String, emailType: String = "ALL"): Boolean {
        return try {
            val unsubscribe = unsubscribeRepository.findByEmail(email)
            if (unsubscribe == null) {
                false
            } else {
                unsubscribe.emailType == "ALL" || unsubscribe.emailType == emailType
            }
        } catch (e: Exception) {
            logger.error("Error checking unsubscribe status for $email", e)
            false
        }
    }

    /**
     * Unsubscribe an email address from emails
     */
    fun unsubscribe(email: String, reason: String? = null, emailType: String = "ALL"): Boolean {
        return try {
            val existing = unsubscribeRepository.findByEmail(email)
            if (existing != null) {
                logger.info("Email $email is already unsubscribed")
                return true
            }

            val unsubscribe = EmailUnsubscribe.fromEmail(email, reason, emailType)
            unsubscribeRepository.save(unsubscribe)
            logger.info("Email $email unsubscribed from $emailType emails")
            true
        } catch (e: Exception) {
            logger.error("Error unsubscribing email $email", e)
            false
        }
    }

    /**
     * Resubscribe an email address
     */
    fun resubscribe(email: String): Boolean {
        return try {
            val unsubscribe = unsubscribeRepository.findByEmail(email)
            if (unsubscribe != null) {
                unsubscribeRepository.delete(unsubscribe)
                logger.info("Email $email resubscribed")
                true
            } else {
                logger.info("Email $email was not unsubscribed")
                true
            }
        } catch (e: Exception) {
            logger.error("Error resubscribing email $email", e)
            false
        }
    }

    /**
     * Get unsubscribe status for an email
     */
    fun getUnsubscribeStatus(email: String): EmailUnsubscribe? {
        return try {
            unsubscribeRepository.findByEmail(email)
        } catch (e: Exception) {
            logger.error("Error getting unsubscribe status for $email", e)
            null
        }
    }

    /**
     * Check if email should receive marketing emails
     */
    fun canSendMarketing(email: String): Boolean {
        val unsubscribe = getUnsubscribeStatus(email) ?: return true
        return unsubscribe.emailType != "ALL" && unsubscribe.emailType != "PROMOTIONAL"
    }

    /**
     * Check if email should receive transactional emails (booking confirmations, etc.)
     */
    fun canSendTransactional(email: String): Boolean {
        val unsubscribe = getUnsubscribeStatus(email) ?: return true
        // Transactional emails should always be sent unless completely unsubscribed
        return unsubscribe.emailType != "ALL"
    }
}
