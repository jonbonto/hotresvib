package com.hotresvib.infrastructure.security

import com.hotresvib.domain.user.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.util.Date
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

    fun generateAccessToken(user: User, expiresInSeconds: Long = 15 * 60): JwtToken {
        val now = Instant.now(clock)
        val expiry = now.plusSeconds(expiresInSeconds)
        val token = Jwts.builder()
            .subject(user.id.value.toString())
            .claim("role", user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()
        return JwtToken(token, expiry)
    }

    fun generateRefreshToken(user: User, expiresInSeconds: Long = 7 * 24 * 60 * 60): JwtToken {
        val now = Instant.now(clock)
        val expiry = now.plusSeconds(expiresInSeconds)
        val token = Jwts.builder()
            .subject(user.id.value.toString())
            .claim("role", user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(key)
            .compact()
        return JwtToken(token, expiry)
    }

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
}

data class JwtToken(val value: String, val expiresAt: Instant)
