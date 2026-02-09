package com.hotresvib.infrastructure.audit

import com.hotresvib.application.port.AuditLogRepository
import com.hotresvib.domain.audit.AuditLog
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Service for logging security-sensitive operations.
 * Logs: authentication attempts, reservations, payments, admin operations.
 */
@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository
) {
    
    fun logAuthenticationAttempt(
        userId: String?,
        success: Boolean,
        request: HttpServletRequest
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = "LOGIN",
            resourceType = "USER",
            resourceId = userId,
            status = if (success) "SUCCESS" else "FAILURE",
            ipAddress = getClientIP(request),
            userAgent = request.getHeader("User-Agent"),
            details = if (success) "Login successful" else "Login failed - invalid credentials"
        )
        auditLogRepository.save(auditLog)
    }
    
    fun logRegistration(
        userId: String,
        email: String,
        request: HttpServletRequest
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = "REGISTER",
            resourceType = "USER",
            resourceId = userId,
            status = "SUCCESS",
            ipAddress = getClientIP(request),
            userAgent = request.getHeader("User-Agent"),
            details = "User registered with email: $email"
        )
        auditLogRepository.save(auditLog)
    }
    
    fun logReservationCreated(
        userId: String,
        reservationId: String,
        hotelId: String,
        roomId: String,
        request: HttpServletRequest? = null
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = "CREATE_RESERVATION",
            resourceType = "RESERVATION",
            resourceId = reservationId,
            status = "SUCCESS",
            ipAddress = request?.let { getClientIP(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = "Reservation created for hotel=$hotelId, room=$roomId"
        )
        auditLogRepository.save(auditLog)
    }
    
    fun logReservationCancelled(
        userId: String,
        reservationId: String,
        request: HttpServletRequest? = null
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = "CANCEL_RESERVATION",
            resourceType = "RESERVATION",
            resourceId = reservationId,
            status = "SUCCESS",
            ipAddress = request?.let { getClientIP(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = "Reservation cancelled"
        )
        auditLogRepository.save(auditLog)
    }
    
    fun logPaymentProcessed(
        userId: String,
        paymentId: String,
        reservationId: String,
        amount: Double,
        success: Boolean,
        request: HttpServletRequest? = null
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = "PAYMENT",
            resourceType = "PAYMENT",
            resourceId = paymentId,
            status = if (success) "SUCCESS" else "FAILURE",
            ipAddress = request?.let { getClientIP(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = "Payment processed for reservation=$reservationId, amount=$amount"
        )
        auditLogRepository.save(auditLog)
    }
    
    fun logAdminAction(
        userId: String,
        action: String,
        resourceType: String,
        resourceId: String?,
        success: Boolean,
        details: String? = null,
        request: HttpServletRequest? = null
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = action,
            resourceType = resourceType,
            resourceId = resourceId,
            status = if (success) "SUCCESS" else "FAILURE",
            ipAddress = request?.let { getClientIP(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = details
        )
        auditLogRepository.save(auditLog)
    }
    
    fun logSecurityEvent(
        userId: String?,
        action: String,
        success: Boolean,
        details: String,
        request: HttpServletRequest? = null
    ) {
        val auditLog = AuditLog(
            timestamp = Instant.now(),
            userId = userId,
            action = action,
            resourceType = "SECURITY",
            resourceId = null,
            status = if (success) "SUCCESS" else "FAILURE",
            ipAddress = request?.let { getClientIP(it) },
            userAgent = request?.getHeader("User-Agent"),
            details = details
        )
        auditLogRepository.save(auditLog)
    }
    
    private fun getClientIP(request: HttpServletRequest): String {
        return request.getHeader("X-Forwarded-For")
            ?.split(",")?.getOrNull(0)?.trim()
            ?: request.remoteAddr
            ?: "unknown"
    }
}
