package com.hotresvib.application.service

/**
 * Service for secure password hashing and verification
 */
interface PasswordHashingService {
    /**
     * Hash a plain text password
     * @param plainPassword the password to hash
     * @return the hashed password
     */
    fun hashPassword(plainPassword: String): String

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword the password to verify
     * @param hashedPassword the hashed password to compare against
     * @return true if passwords match, false otherwise
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean
}
