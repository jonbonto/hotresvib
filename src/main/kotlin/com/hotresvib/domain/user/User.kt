package com.hotresvib.domain.user

import com.hotresvib.domain.shared.UserId

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

data class User(
    val id: UserId,
    val email: EmailAddress,
    val displayName: String,
    val role: UserRole,
    val passwordHash: String
) {
    init {
        require(displayName.isNotBlank()) { "Display name is required" }
        require(passwordHash.isNotBlank()) { "Password hash is required" }
    }
}
