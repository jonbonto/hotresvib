package com.hotresvib.domain.user

import com.hotresvib.domain.shared.UserId
import jakarta.persistence.*
import java.time.Instant
import java.time.ZoneId

@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(value.isNotBlank()) { "Email is required" }
        require(EMAIL_REGEX.matches(value)) { "Invalid email format" }
    }

    private companion object {
        private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    }
}

enum class UserRole {
    CUSTOMER,
    STAFF,
    ADMIN
}

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: UserId,

    @Column(name = "email", nullable = false, unique = true)
    val email: EmailAddress,

    @Column(name = "display_name", nullable = false)
    val displayName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    val role: UserRole,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    
    // Phase 11: Account lockout
    @Column(name = "failed_login_attempts", nullable = false)
    val failedLoginAttempts: Int = 0,
    
    @Column(name = "locked_until")
    val lockedUntil: Instant? = null,
    
    // Phase 11: Timezone handling
    @Column(name = "timezone", nullable = false)
    val timezone: String = "UTC",
    
    // Phase 11: Optimistic locking
    @Version
    @Column(name = "version")
    val version: Long? = null
) {
    init {
        require(displayName.isNotBlank()) { "Display name is required" }
        require(passwordHash.isNotBlank()) { "Password hash is required" }
        require(failedLoginAttempts >= 0) { "Failed login attempts must be non-negative" }
    }
    
    fun isAccountLocked(): Boolean {
        return lockedUntil != null && lockedUntil > Instant.now()
    }
    
    fun withFailedLoginAttempt(): User {
        return if (failedLoginAttempts >= 4) {
            // Lock account for 30 minutes after 5 failed attempts
            copy(failedLoginAttempts = failedLoginAttempts + 1, lockedUntil = Instant.now().plusSeconds(1800))
        } else {
            copy(failedLoginAttempts = failedLoginAttempts + 1)
        }
    }
    
    fun withResetLoginAttempts(): User {
        return copy(failedLoginAttempts = 0, lockedUntil = null)
    }
}
