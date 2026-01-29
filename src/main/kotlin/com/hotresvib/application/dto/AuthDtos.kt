package com.hotresvib.application.dto

import com.hotresvib.domain.user.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Request DTO for user registration
 */
data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,

    @field:NotBlank(message = "Display name is required")
    val displayName: String
)

/**
 * Request DTO for user login
 */
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * Request DTO for token refresh
 */
data class RefreshRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

/**
 * Request DTO for updating user profile
 */
data class UpdateProfileRequest(
    @field:NotBlank(message = "Display name is required")
    val displayName: String
)

/**
 * Response DTO for authentication (login/register)
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

/**
 * Response DTO for token refresh
 */
data class RefreshResponse(
    val accessToken: String
)

/**
 * Response DTO for user information
 */
data class UserResponse(
    val id: UUID,
    val email: String,
    val displayName: String,
    val role: UserRole
)

/**
 * Response DTO for successful registration (without tokens)
 */
data class RegisterResponse(
    val message: String,
    val user: UserResponse
)

/**
 * Response DTO for logout
 */
data class LogoutResponse(
    val message: String
)

/**
 * Legacy response for backward compatibility
 */
@Deprecated("Use AuthResponse instead")
data class LoginResponse(
    val token: String,
    val userId: UUID,
    val email: String,
    val displayName: String
)
