package com.hotresvib.application.service

import com.hotresvib.application.dto.*
import com.hotresvib.application.port.RefreshTokenRepository
import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.auth.RefreshToken
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.security.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service for handling user authentication operations
 */
@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHashingService: PasswordHashingService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * Register a new user
     */
    @Transactional
    fun register(request: RegisterRequest): RegisterResponse {
        // Check if email already exists
        val email = EmailAddress(request.email)
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            throw IllegalArgumentException("Email already registered")
        }

        // Hash password
        val hashedPassword = passwordHashingService.hashPassword(request.password)

        // Create new user with CUSTOMER role
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = email,
            displayName = request.displayName,
            role = UserRole.CUSTOMER,
            passwordHash = hashedPassword
        )

        // Save user
        val savedUser = userRepository.save(user)

        return RegisterResponse(
            message = "Registration successful",
            user = UserResponse(
                id = savedUser.id.value,
                email = savedUser.email.value,
                displayName = savedUser.displayName,
                role = savedUser.role
            )
        )
    }

    /**
     * Login a user and generate tokens
     */
    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        // Find user by email
        val email = EmailAddress(request.email)
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Invalid email or password")

        // Verify password
        if (!passwordHashingService.verifyPassword(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        // Generate access token
        val accessToken = jwtTokenProvider.generateToken(user.id, user.email.value, user.role)

        // Generate refresh token
        val refreshTokenString = UUID.randomUUID().toString()
        val refreshToken = RefreshToken.create(user.id, refreshTokenString)
        refreshTokenRepository.save(refreshToken)

        return AuthResponse(
            accessToken = accessToken.value,
            refreshToken = refreshTokenString,
            user = UserResponse(
                id = user.id.value,
                email = user.email.value,
                displayName = user.displayName,
                role = user.role
            )
        )
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    fun refresh(request: RefreshRequest): RefreshResponse {
        // Find refresh token
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw IllegalArgumentException("Invalid refresh token")

        // Check if expired
        if (refreshToken.isExpired()) {
            refreshTokenRepository.deleteById(refreshToken.id)
            throw IllegalArgumentException("Refresh token expired")
        }

        // Find user
        val user = userRepository.findById(refreshToken.userId)
            ?: throw IllegalArgumentException("User not found")

        // Generate new access token
        val accessToken = jwtTokenProvider.generateToken(user.id, user.email.value, user.role)

        return RefreshResponse(
            accessToken = accessToken.value
        )
    }

    /**
     * Logout user by deleting their refresh tokens
     */
    @Transactional
    fun logout(userId: UserId): LogoutResponse {
        refreshTokenRepository.deleteByUserId(userId)
        return LogoutResponse(message = "Logged out successfully")
    }

    /**
     * Get user profile
     */
    fun getUserProfile(userId: UserId): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        return UserResponse(
            id = user.id.value,
            email = user.email.value,
            displayName = user.displayName,
            role = user.role
        )
    }

    /**
     * Update user profile
     */
    @Transactional
    fun updateProfile(userId: UserId, request: UpdateProfileRequest): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        val updatedUser = user.copy(displayName = request.displayName)
        val savedUser = userRepository.save(updatedUser)

        return UserResponse(
            id = savedUser.id.value,
            email = savedUser.email.value,
            displayName = savedUser.displayName,
            role = savedUser.role
        )
    }
}
