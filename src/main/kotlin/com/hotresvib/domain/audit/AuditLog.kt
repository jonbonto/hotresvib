package com.hotresvib.domain.audit

import jakarta.persistence.*
import java.time.Instant

/**
 * Entity for storing audit logs of security-sensitive operations.
 * Tracks: authentication, reservations, payments, admin operations.
 */
@Entity
@Table(name = "audit_logs")
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    
    @Column(name = "timestamp", nullable = false)
    val timestamp: Instant,
    
    @Column(name = "user_id")
    val userId: String?,
    
    @Column(name = "action", nullable = false)
    val action: String,
    
    @Column(name = "resource_type", nullable = false)
    val resourceType: String,
    
    @Column(name = "resource_id")
    val resourceId: String?,
    
    @Column(name = "status", nullable = false)
    val status: String,
    
    @Column(name = "ip_address")
    val ipAddress: String?,
    
    @Column(name = "user_agent")
    val userAgent: String?,
    
    @Column(name = "details", columnDefinition = "TEXT")
    val details: String?
) {
    init {
        require(action.isNotBlank()) { "Action is required" }
        require(resourceType.isNotBlank()) { "Resource type is required" }
        require(status.isNotBlank()) { "Status is required" }
    }
}
