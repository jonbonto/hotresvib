package com.hotresvib.domain.auth

import com.hotresvib.domain.shared.UserId
import java.time.Instant
import java.util.UUID

/**
 * RefreshToken entity for managing JWT refresh tokens
 * Enables token rotation and long-lived sessions
 */
data class RefreshToken(
    val id: UUID,
    val token: String,
    val userId: UserId,
    val expiresAt: Instant,
    val createdAt: Instant
) {
    init {
        require(token.isNotBlank()) { "Token cannot be blank" }
        require(expiresAt.isAfter(createdAt)) { "Expiration must be after creation" }
    }

    /**
     * Check if token is expired
     */
    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

    /**
     * Check if token is valid (not expired)
     */
    fun isValid(): Boolean = !isExpired()

    companion object {
        /**
         * Create a new refresh token with 7 days expiration
         */
        fun create(userId: UserId, token: String): RefreshToken {
            val now = Instant.now()
            val expiresAt = now.plusSeconds(7 * 24 * 60 * 60) // 7 days
            return RefreshToken(
                id = UUID.randomUUID(),
                token = token,
                userId = userId,
                expiresAt = expiresAt,
                createdAt = now
            )
        }
    }
}
