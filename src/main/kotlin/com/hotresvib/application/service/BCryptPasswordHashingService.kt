package com.hotresvib.application.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

/**
 * BCrypt implementation of PasswordHashingService
 * Uses BCrypt with strength 12 for secure password hashing
 */
@Service
class BCryptPasswordHashingService : PasswordHashingService {
    private val encoder = BCryptPasswordEncoder(12)

    override fun hashPassword(plainPassword: String): String {
        require(plainPassword.isNotBlank()) { "Password cannot be blank" }
        require(plainPassword.length >= 8) { "Password must be at least 8 characters" }
        return encoder.encode(plainPassword)
    }

    override fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        require(plainPassword.isNotBlank()) { "Password cannot be blank" }
        require(hashedPassword.isNotBlank()) { "Hashed password cannot be blank" }
        return try {
            encoder.matches(plainPassword, hashedPassword)
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
