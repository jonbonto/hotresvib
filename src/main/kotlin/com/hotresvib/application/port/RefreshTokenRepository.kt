package com.hotresvib.application.port

import com.hotresvib.domain.auth.RefreshToken
import com.hotresvib.domain.shared.UserId
import java.util.UUID

/**
 * Repository interface for RefreshToken persistence
 */
interface RefreshTokenRepository {
    /**
     * Save a refresh token
     */
    fun save(token: RefreshToken): RefreshToken

    /**
     * Find refresh token by token string
     */
    fun findByToken(token: String): RefreshToken?

    /**
     * Delete all refresh tokens for a user (logout all devices)
     */
    fun deleteByUserId(userId: UserId)

    /**
     * Delete expired tokens (cleanup)
     */
    fun deleteExpired()

    /**
     * Find all tokens for a user
     */
    fun findByUserId(userId: UserId): List<RefreshToken>

    /**
     * Delete a specific token by ID
     */
    fun deleteById(id: UUID)
}
