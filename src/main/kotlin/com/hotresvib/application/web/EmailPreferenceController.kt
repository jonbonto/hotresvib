package com.hotresvib.application.web

import com.hotresvib.application.notification.EmailPreferenceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import org.slf4j.LoggerFactory

/**
 * REST controller for managing email preferences
 */
@RestController
@RequestMapping("/api/email")
class EmailPreferenceController(
    private val emailPreferenceService: EmailPreferenceService,
    private val userRepository: com.hotresvib.application.port.UserRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Unsubscribe an email from emails
     * POST /api/email/unsubscribe?email=test@example.com
     */
    @PostMapping("/unsubscribe")
    fun unsubscribe(
        @RequestParam email: String,
        @RequestParam(required = false) reason: String? = null,
        @RequestParam(required = false, defaultValue = "ALL") emailType: String = "ALL"
    ): ResponseEntity<Map<String, String>> {
        return try {
            val success = emailPreferenceService.unsubscribe(email, reason, emailType)
            if (success) {
                logger.info("Email unsubscribed: $email")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "You have been unsubscribed from emails."
                ))
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to unsubscribe")
            }
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error unsubscribing email: $email", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request")
        }
    }

    /**
     * Resubscribe an email
     * POST /api/email/resubscribe?email=test@example.com
     */
    @PostMapping("/resubscribe")
    fun resubscribe(@RequestParam email: String): ResponseEntity<Map<String, String>> {
        return try {
            val success = emailPreferenceService.resubscribe(email)
            if (success) {
                logger.info("Email resubscribed: $email")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "You have been resubscribed to emails."
                ))
            } else {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to resubscribe")
            }
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error resubscribing email: $email", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request")
        }
    }

    /**
     * Check subscription status
     * GET /api/email/status?email=test@example.com
     */
    @GetMapping("/status")
    fun checkStatus(@RequestParam email: String): ResponseEntity<Map<String, Any>> {
        return try {
            val unsubscribe = emailPreferenceService.getUnsubscribeStatus(email)
            val response = if (unsubscribe != null) {
                mapOf(
                    "email" to email,
                    "isUnsubscribed" to true,
                    "emailType" to unsubscribe.emailType,
                    "reason" to (unsubscribe.reason ?: "Not provided"),
                    "unsubscribedAt" to unsubscribe.unsubscribedAt.toString()
                )
            } else {
                mapOf(
                    "email" to email,
                    "isUnsubscribed" to false,
                    "emailType" to "SUBSCRIBED"
                )
            }
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Error checking status for email: $email", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request")
        }
    }

    /**
     * One-click unsubscribe via token link
     * GET /api/email/unsubscribe/{token}
     */
    @GetMapping("/unsubscribe/{token}")
    fun unsubscribeByToken(@PathVariable token: String): ResponseEntity<Map<String, String>> {
        return try {
            val user = userRepository.findByUnsubscribeToken(token)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid unsubscribe token")

            // Mark user as opted out for marketing and record unsubscribe entry
            val email = user.email.value
            val success = emailPreferenceService.unsubscribe(email, "Unsubscribed via link", "ALL")

            // Persist user preference change
            val updated = user.withMarketingOptOut()
            userRepository.save(updated)

            if (success) {
                logger.info("User unsubscribed via token: ${user.email.value}")
                ResponseEntity.ok(mapOf(
                    "status" to "success",
                    "message" to "You have been unsubscribed from marketing emails."
                ))
            } else {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to unsubscribe")
            }
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            logger.error("Error processing unsubscribe token: $token", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing your request")
        }
    }
}
