package com.hotresvib.domain.user

import com.hotresvib.domain.shared.UserId

@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(value.isNotBlank()) { "Email is required" }
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
