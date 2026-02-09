package com.hotresvib.application.port

import com.hotresvib.domain.audit.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, String> {
    fun findByUserIdAndTimestampAfter(userId: String, timestamp: Instant): List<AuditLog>
    fun findByActionAndStatus(action: String, status: String): List<AuditLog>
    fun findByResourceTypeAndTimestampAfter(resourceType: String, timestamp: Instant): List<AuditLog>
}
