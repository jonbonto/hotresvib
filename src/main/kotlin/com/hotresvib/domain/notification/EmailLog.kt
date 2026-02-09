package com.hotresvib.domain.notification

import java.time.Instant
import java.util.*
import jakarta.persistence.*

/**
 * Email audit log for all email send attempts
 */
@Entity
@Table(
    name = "email_logs",
    indexes = [
        Index(name = "idx_email_logs_recipient", columnList = "recipient_email"),
        Index(name = "idx_email_logs_sent_at", columnList = "sent_at"),
        Index(name = "idx_email_logs_status", columnList = "status"),
        Index(name = "idx_email_logs_template", columnList = "template_name")
    ]
)
data class EmailLog(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Column(nullable = false, length = 255)
    val recipientEmail: String,

    @Column(nullable = false, length = 255)
    val subject: String,

    @Column(nullable = false, length = 100)
    val templateName: String,

    @Column(nullable = false)
    val sentAt: Instant = Instant.now(),

    @Column(nullable = false, length = 20)
    val status: String, // SUCCESS, FAILURE

    @Column(columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    companion object {
        fun success(recipientEmail: String, subject: String, templateName: String): EmailLog {
            return EmailLog(
                recipientEmail = recipientEmail,
                subject = subject,
                templateName = templateName,
                status = "SUCCESS"
            )
        }

        fun failure(recipientEmail: String, subject: String, templateName: String, error: String): EmailLog {
            return EmailLog(
                recipientEmail = recipientEmail,
                subject = subject,
                templateName = templateName,
                status = "FAILURE",
                errorMessage = error
            )
        }
    }
}
