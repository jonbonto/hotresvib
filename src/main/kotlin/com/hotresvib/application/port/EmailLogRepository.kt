package com.hotresvib.application.port

import com.hotresvib.domain.notification.EmailLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface EmailLogRepository : JpaRepository<EmailLog, String> {
    fun findByRecipientEmailOrderBySentAtDesc(recipientEmail: String): List<EmailLog>
    fun findByStatusOrderBySentAtDesc(status: String): List<EmailLog>
    fun findBySentAtGreaterThanOrderBySentAtDesc(sentAt: Instant): List<EmailLog>
    fun findByTemplateNameAndStatusOrderBySentAtDesc(templateName: String, status: String): List<EmailLog>
}
