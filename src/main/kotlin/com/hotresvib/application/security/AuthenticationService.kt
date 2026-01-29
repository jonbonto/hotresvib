package com.hotresvib.application.security

import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.infrastructure.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun authenticate(email: String, password: String): String {
        val user = userRepository.findByEmail(EmailAddress(email))
            ?: throw IllegalArgumentException("User not found")

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val access = jwtTokenProvider.generateAccessToken(user)
        return access.value
    }
}
