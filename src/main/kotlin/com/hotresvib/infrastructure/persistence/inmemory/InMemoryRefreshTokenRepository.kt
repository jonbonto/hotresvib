package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.RefreshTokenRepository
import com.hotresvib.domain.auth.RefreshToken
import com.hotresvib.domain.shared.UserId
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of RefreshTokenRepository
 * Thread-safe for concurrent access
 */
@Repository
class InMemoryRefreshTokenRepository : RefreshTokenRepository {
    private val tokens = ConcurrentHashMap<UUID, RefreshToken>()
    private val tokenIndex = ConcurrentHashMap<String, UUID>() // token string -> id

    override fun save(token: RefreshToken): RefreshToken {
        tokens[token.id] = token
        tokenIndex[token.token] = token.id
        return token
    }

    override fun findByToken(token: String): RefreshToken? {
        val id = tokenIndex[token] ?: return null
        return tokens[id]
    }

    override fun deleteByUserId(userId: UserId) {
        val toDelete = tokens.values.filter { it.userId == userId }
        toDelete.forEach { token ->
            tokens.remove(token.id)
            tokenIndex.remove(token.token)
        }
    }

    override fun deleteExpired() {
        val now = Instant.now()
        val expired = tokens.values.filter { it.expiresAt.isBefore(now) }
        expired.forEach { token ->
            tokens.remove(token.id)
            tokenIndex.remove(token.token)
        }
    }

    override fun findByUserId(userId: UserId): List<RefreshToken> {
        return tokens.values.filter { it.userId == userId }
    }

    override fun deleteById(id: UUID) {
        val token = tokens.remove(id)
        token?.let { tokenIndex.remove(it.token) }
    }
}
