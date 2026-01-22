package com.hotresvib.infrastructure.security

import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class JwtTokenProviderTest {

    private val provider = JwtTokenProvider("0123456789abcdef0123456789abcdef", Clock.systemUTC())

    @Test
    fun `generates access and refresh tokens with role claim`() {
        val user = User(
            id = UserId.generate(),
            email = EmailAddress("user@example.com"),
            displayName = "User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        val access = provider.generateAccessToken(user, expiresInSeconds = 60)
        val refresh = provider.generateRefreshToken(user, expiresInSeconds = 120)

        assertThat(access.value).isNotBlank()
        assertThat(refresh.value).isNotBlank()
        val now = Instant.now(Clock.systemUTC())
        assertThat(access.expiresAt).isAfter(now)
        assertThat(refresh.expiresAt).isAfter(access.expiresAt)

        val parser = Jwts.parser().verifyWith(Keys.hmacShaKeyFor("0123456789abcdef0123456789abcdef".toByteArray())).build()
        val accessClaims = parser.parseSignedClaims(access.value).payload
        assertThat(accessClaims.subject).isEqualTo(user.id.value.toString())
        assertThat(accessClaims["role"]).isEqualTo("CUSTOMER")
    }
}
