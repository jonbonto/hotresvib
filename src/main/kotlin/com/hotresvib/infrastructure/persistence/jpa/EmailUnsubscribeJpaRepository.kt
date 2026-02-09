package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.notification.EmailUnsubscribe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailUnsubscribeJpaRepository : JpaRepository<EmailUnsubscribe, String> {
    fun findByEmail(email: String): EmailUnsubscribe?
    fun existsByEmail(email: String): Boolean
    fun findByEmailType(emailType: String): List<EmailUnsubscribe>
}
