package com.hotresvib.infrastructure.security

import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.domain.shared.UserId
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtTokenProvider(
    @Value("\${security.jwt.secret}") secret: String,
    private val clock: Clock = Clock.systemUTC()
) {
    private val key: SecretKey

    init {
        val secretBytes = secret.toByteArray(StandardCharsets.UTF_8)
        require(secretBytes.size >= 32) {
            "JWT secret key must be at least 32 bytes (256 bits) when encoded as UTF-8 for HMAC-SHA256"
        }
        key = Keys.hmacShaKeyFor(secretBytes)
    }

    /**
     * Generate JWT access token with userId, email, and role claims
     * @param userId the user's ID
     * @param email the user's email
     * @param role the user's role
     * @param expiresInSeconds token expiration (default 1 hour)
     * @return JWT token
     */
    fun generateToken(userId: UserId, email: String, role: UserRole, expiresInSeconds: Long = 60 * 60): JwtToken {
        val now = Instant.now(clock)
        val expiry = now.plusSeconds(expiresInSeconds)
        val token = Jwts.builder()
            .subject(userId.value.toString())
            .claim("email", email)
            .claim("role", role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()
        return JwtToken(token, expiry)
    }

    /**
     * Generate access token from User entity (backward compatibility)
     */
    fun generateAccessToken(user: User, expiresInSeconds: Long = 60 * 60): JwtToken {
        return generateToken(user.id, user.email.value, user.role, expiresInSeconds)
    }

    /**
     * Generate refresh token from User entity
     */
    fun generateRefreshToken(user: User, expiresInSeconds: Long = 7 * 24 * 60 * 60): JwtToken {
        return generateToken(user.id, user.email.value, user.role, expiresInSeconds)
    }

    /**
     * Validate JWT token
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get subject (userId) from token
     */
    fun getSubjectFromToken(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get userId from token
     */
    fun getUserIdFromToken(token: String): UserId? {
        return try {
            val subject = getSubjectFromToken(token) ?: return null
            UserId(UUID.fromString(subject))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get email from token
     */
    fun getEmailFromToken(token: String): String? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .get("email", String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get role from token
     */
    fun getRoleFromToken(token: String): UserRole? {
        return try {
            val roleString = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
                .get("role", String::class.java)
            UserRole.valueOf(roleString)
        } catch (e: Exception) {
            null
        }
    }
}

data class JwtToken(val value: String, val expiresAt: Instant)
