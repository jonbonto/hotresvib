package com.hotresvib.application.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class BCryptPasswordHashingServiceTest {

    private val service = BCryptPasswordHashingService()

    @Test
    fun `should hash password successfully`() {
        val plainPassword = "MySecurePassword123"
        val hashed = service.hashPassword(plainPassword)

        assertTrue(hashed.isNotBlank())
        assertTrue(hashed.startsWith("\$2a\$") || hashed.startsWith("\$2b\$"))
        assertTrue(hashed.length >= 60) // BCrypt hash length
    }

    @Test
    fun `should verify correct password`() {
        val plainPassword = "MySecurePassword123"
        val hashed = service.hashPassword(plainPassword)

        val result = service.verifyPassword(plainPassword, hashed)

        assertTrue(result)
    }

    @Test
    fun `should reject incorrect password`() {
        val plainPassword = "MySecurePassword123"
        val wrongPassword = "WrongPassword456"
        val hashed = service.hashPassword(plainPassword)

        val result = service.verifyPassword(wrongPassword, hashed)

        assertFalse(result)
    }

    @Test
    fun `should throw exception for password less than 8 characters`() {
        val shortPassword = "Short1"

        val exception = assertThrows<IllegalArgumentException> {
            service.hashPassword(shortPassword)
        }

        assertEquals("Password must be at least 8 characters", exception.message)
    }

    @Test
    fun `should throw exception for blank password`() {
        assertThrows<IllegalArgumentException> {
            service.hashPassword("")
        }

        assertThrows<IllegalArgumentException> {
            service.hashPassword("   ")
        }
    }

    @Test
    fun `should throw exception for blank hashed password in verify`() {
        assertThrows<IllegalArgumentException> {
            service.verifyPassword("password", "")
        }
    }

    @Test
    fun `should return false for invalid hash format`() {
        val result = service.verifyPassword("password", "invalid-hash")

        assertFalse(result)
    }

    @Test
    fun `should generate different hashes for same password`() {
        val plainPassword = "MySecurePassword123"
        val hash1 = service.hashPassword(plainPassword)
        val hash2 = service.hashPassword(plainPassword)

        // BCrypt includes random salt, so hashes should differ
        assertTrue(hash1 != hash2)
        
        // But both should verify successfully
        assertTrue(service.verifyPassword(plainPassword, hash1))
        assertTrue(service.verifyPassword(plainPassword, hash2))
    }
}
