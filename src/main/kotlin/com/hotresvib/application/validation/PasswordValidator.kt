package com.hotresvib.application.validation

import org.springframework.stereotype.Service

/**
 * Validator for strong password requirements.
 * Enforces: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
 * Also checks against common passwords.
 */
@Service
class PasswordValidator {
    
    private val commonPasswords = setOf(
        "password", "123456", "12345678", "qwerty", "abc123", "monkey", "1234567", "letmein",
        "trustno1", "dragon", "baseball", "111111", "iloveyou", "master", "sunshine", "ashley",
        "bailey", "shadow", "123123", "654321", "superman", "qazwsx", "michael", "football",
        "hello", "princess", "welcome", "login", "passw0rd", "admin", "root", "toor",
        "test", "guest", "changeme", "password1", "123456789", "welcome123"
    )
    
    fun validate(password: String): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Minimum length
        if (password.length < 8) {
            errors.add("Password must be at least 8 characters long")
        }
        
        // Maximum length
        if (password.length > 128) {
            errors.add("Password must not exceed 128 characters")
        }
        
        // At least one uppercase
        if (!password.any { it.isUpperCase() }) {
            errors.add("Password must contain at least one uppercase letter")
        }
        
        // At least one lowercase
        if (!password.any { it.isLowerCase() }) {
            errors.add("Password must contain at least one lowercase letter")
        }
        
        // At least one digit
        if (!password.any { it.isDigit() }) {
            errors.add("Password must contain at least one digit")
        }
        
        // At least one special character
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("Password must contain at least one special character (!@#$%^&*)")
        }
        
        // Check against common passwords
        if (commonPasswords.contains(password.lowercase())) {
            errors.add("Password is too common. Please choose a more unique password")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult(true, emptyList())
        } else {
            ValidationResult(false, errors)
        }
    }
    
    fun getStrength(password: String): PasswordStrength {
        var score = 0
        
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        
        return when {
            score <= 2 -> PasswordStrength.WEAK
            score <= 4 -> PasswordStrength.FAIR
            score <= 5 -> PasswordStrength.GOOD
            else -> PasswordStrength.STRONG
        }
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
    
    enum class PasswordStrength {
        WEAK, FAIR, GOOD, STRONG
    }
}
