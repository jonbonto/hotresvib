package com.hotresvib.application.service

import com.hotresvib.application.dto.*
import com.hotresvib.application.port.RefreshTokenRepository
import com.hotresvib.application.port.UserRepository
import com.hotresvib.application.validation.PasswordValidator
import com.hotresvib.domain.auth.RefreshToken
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.audit.AuditLogService
import com.hotresvib.infrastructure.security.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
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
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordValidator: PasswordValidator,
    private val auditLogService: AuditLogService
) {

    /**
     * Register a new user
     */
    @Transactional
    fun register(request: RegisterRequest, httpRequest: HttpServletRequest? = null): RegisterResponse {
        // Check if email already exists
        val email = EmailAddress(request.email)
        val existingUser = userRepository.findByEmail(email)
        if (existingUser != null) {
            httpRequest?.let {
                auditLogService.logRegistration(
                    userId = "unknown",
                    email = request.email,
                    request = it
                )
            }
            throw IllegalArgumentException("Email already registered")
        }
        
        // Phase 11: Validate password strength
        val passwordValidation = passwordValidator.validate(request.password)
        if (!passwordValidation.isValid) {
            throw IllegalArgumentException(
                "Password does not meet requirements: ${passwordValidation.errors.joinToString(", ")}"
            )
        }

        // Hash password
        val hashedPassword = passwordHashingService.hashPassword(request.password)

        // Create new user with CUSTOMER role
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = email,
            displayName = request.displayName,
            role = UserRole.CUSTOMER,
            passwordHash = hashedPassword,
            failedLoginAttempts = 0,
            lockedUntil = null,
            timezone = "UTC"
        )

        // Save user
        val savedUser = userRepository.save(user)
        
        // Phase 11: Audit log registration
        httpRequest?.let { auditLogService.logRegistration(savedUser.id.value.toString(), request.email, it) }

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
    fun login(request: LoginRequest, httpRequest: HttpServletRequest? = null): AuthResponse {
        // Find user by email
        val email = EmailAddress(request.email)
        val user = userRepository.findByEmail(email)
            ?: {
                httpRequest?.let { auditLogService.logAuthenticationAttempt(null, false, it) }
                throw IllegalArgumentException("Invalid email or password")
            }()
        
        // Phase 11: Check if account is locked
        if (user.isAccountLocked()) {
            httpRequest?.let { auditLogService.logSecurityEvent(user.id.value.toString(), "LOGIN_ATTEMPT_LOCKED_ACCOUNT", false, "Account is locked", it) }
            throw IllegalArgumentException("Account is locked due to too many failed login attempts. Please try again later.")
        }

        // Verify password
        if (!passwordHashingService.verifyPassword(request.password, user.passwordHash)) {
            // Phase 11: Increment failed login attempts
            val updatedUser = user.withFailedLoginAttempt()
            userRepository.save(updatedUser)
            httpRequest?.let { auditLogService.logAuthenticationAttempt(user.id.value.toString(), false, it) }
            throw IllegalArgumentException("Invalid email or password")
        }
        
        // Phase 11: Reset failed login attempts on successful login
        val resetUser = user.withResetLoginAttempts()
        userRepository.save(resetUser)

        // Generate access token
        val accessToken = jwtTokenProvider.generateToken(user.id, user.email.value, user.role)

        // Generate refresh token
        val refreshTokenString = UUID.randomUUID().toString()
        val refreshToken = RefreshToken.create(user.id, refreshTokenString)
        refreshTokenRepository.save(refreshToken)
        
        // Phase 11: Audit log successful login
        httpRequest?.let { auditLogService.logAuthenticationAttempt(user.id.value.toString(), true, it) }

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
