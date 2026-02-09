package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.service.AuthenticationService
import com.hotresvib.domain.shared.UserId
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * REST controller for authentication endpoints
 * Phase 11: Includes rate limiting, account lockout, and audit logging
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["http://localhost:3000", "http://localhost:3001"])
class AuthController(
    private val authenticationService: AuthenticationService
) {

    /**
     * Register a new user
     * POST /api/auth/register
     * Phase 11: Rate limited (3 requests per hour per IP)
     */
    @PostMapping("/register")
    fun register(
        @Valid @RequestBody request: RegisterRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<RegisterResponse> {
        return try {
            val response = authenticationService.register(request, httpRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            when {
                e.message?.contains("already registered") == true ->
                    ResponseEntity.status(HttpStatus.CONFLICT).body(null)
                e.message?.contains("does not meet requirements") == true ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
                e.message?.contains("email", ignoreCase = true) == true ||
                e.message?.contains("password", ignoreCase = true) == true ||
                e.message?.contains("display name", ignoreCase = true) == true ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
                else ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
            }
        }
    }

    /**
     * Login user and get tokens
     * POST /api/auth/login
     * Phase 11: Rate limited (5 requests per minute per IP), includes account lockout check
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        return try {
            val response = authenticationService.login(request, httpRequest)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            when {
                e.message?.contains("locked") == true ->
                    ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
                else ->
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
            }
        }
    }

    /**
     * Refresh access token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<RefreshResponse> {
        return try {
            val response = authenticationService.refresh(request)
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
        }
    }

    /**
     * Logout user (invalidate refresh tokens)
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    fun logout(authentication: Authentication): ResponseEntity<LogoutResponse> {
        return try {
            val userId = UserId(UUID.fromString(authentication.name))
            val response = authenticationService.logout(userId)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    /**
     * Get current user profile
     * GET /api/auth/me
     */
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponse> {
        return try {
            val userId = UserId(UUID.fromString(authentication.name))
            val response = authenticationService.getUserProfile(userId)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }

    /**
     * Update current user profile
     * PUT /api/auth/me
     */
    @PutMapping("/me")
    fun updateCurrentUser(
        authentication: Authentication,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        return try {
            val userId = UserId(UUID.fromString(authentication.name))
            val response = authenticationService.updateProfile(userId, request)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
    }
}
