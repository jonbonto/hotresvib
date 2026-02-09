package com.hotresvib.application.port

import com.hotresvib.domain.notification.EmailUnsubscribe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailUnsubscribeRepository : JpaRepository<EmailUnsubscribe, String> {
    fun findByEmail(email: String): EmailUnsubscribe?
    fun existsByEmail(email: String): Boolean
    fun findByEmailType(emailType: String): List<EmailUnsubscribe>
}
