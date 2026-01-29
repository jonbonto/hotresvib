package com.hotresvib.domain.user

import com.hotresvib.domain.shared.UserId
import jakarta.persistence.*

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
    val passwordHash: String
) {
    init {
        require(displayName.isNotBlank()) { "Display name is required" }
        require(passwordHash.isNotBlank()) { "Password hash is required" }
    }
}
