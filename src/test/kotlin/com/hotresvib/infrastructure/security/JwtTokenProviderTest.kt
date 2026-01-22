package com.hotresvib.infrastructure.security

import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class JwtTokenProviderTest {

    private val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC)
    private val provider = JwtTokenProvider("0123456789abcdef0123456789abcdef", fixedClock)

    @Test
    fun `generates access and refresh tokens with role claim`() {
        val user = User(
            id = UserId.generate(),
            email = EmailAddress("user@example.com"),
            displayName = "User",
            role = UserRole.CUSTOMER
        )

        val access = provider.generateAccessToken(user)
        val refresh = provider.generateRefreshToken(user)

        assertThat(access.value).isNotBlank()
        assertThat(refresh.value).isNotBlank()
        assertThat(access.expiresAt).isAfter(Instant.parse("2024-01-01T00:00:00Z"))
        assertThat(refresh.expiresAt).isAfter(access.expiresAt)
    }
}
