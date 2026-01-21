package com.hotresvib.domain.user

import com.hotresvib.domain.shared.UserId

@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(value.isNotBlank()) { "Email is required" }
        require(EMAIL_REGEX.matches(value)) { "Invalid email format" }
    }

    private companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}

enum class UserRole {
    GUEST,
    STAFF,
    ADMIN
}

data class User(
    val id: UserId,
    val email: EmailAddress,
    val displayName: String,
    val role: UserRole
) {
    init {
        require(displayName.isNotBlank()) { "Display name is required" }
    }
}
