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
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))

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
}

data class JwtToken(val value: String, val expiresAt: Instant)
