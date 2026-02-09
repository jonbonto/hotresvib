package com.hotresvib.domain.notification

import java.util.UUID
import java.time.Instant
import jakarta.persistence.*

/**
 * Entity representing an email unsubscribe record
 */
@Entity
@Table(name = "email_unsubscribes", indexes = [
    Index(name = "idx_unsubscribe_email", columnList = "email"),
    Index(name = "idx_unsubscribe_date", columnList = "unsubscribed_at")
])
data class EmailUnsubscribe(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val reason: String? = null,

    @Column(nullable = false)
    val unsubscribedAt: Instant = Instant.now(),

    @Column(nullable = false)
    val emailType: String = "ALL" // ALL, PROMOTIONAL, NOTIFICATIONS
) {
    companion object {
        fun fromEmail(email: String, reason: String? = null, emailType: String = "ALL"): EmailUnsubscribe {
            return EmailUnsubscribe(
                email = email,
                reason = reason,
                unsubscribedAt = Instant.now(),
                emailType = emailType
            )
        }
    }
}
