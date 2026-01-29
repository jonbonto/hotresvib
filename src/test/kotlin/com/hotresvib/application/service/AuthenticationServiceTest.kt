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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.Instant
import java.util.UUID

class AuthenticationServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var passwordHashingService: PasswordHashingService
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var authenticationService: AuthenticationService

    @BeforeEach
    fun setup() {
        userRepository = mock()
        refreshTokenRepository = mock()
        passwordHashingService = mock()
        // Use real JWT provider with test secret
        jwtTokenProvider = JwtTokenProvider("test-secret-key-minimum-32-bytes-for-hmac-sha256-algorithm")
        authenticationService = AuthenticationService(
            userRepository,
            refreshTokenRepository,
            passwordHashingService,
            jwtTokenProvider
        )
    }

    @Test
    fun `should register new user successfully`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "SecurePass123",
            displayName = "Test User"
        )

        whenever(userRepository.findByEmail(any())).thenReturn(null)
        whenever(passwordHashingService.hashPassword(any())).thenReturn("hashed-password")
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val response = authenticationService.register(request)

        assertEquals("Registration successful", response.message)
        assertEquals(request.email, response.user.email)
        assertEquals(request.displayName, response.user.displayName)
        assertEquals(UserRole.CUSTOMER, response.user.role)
        
        verify(passwordHashingService).hashPassword(any())
        verify(userRepository).save(any())
    }

    @Test
    fun `should throw exception when registering with existing email`() {
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "SecurePass123",
            displayName = "Test User"
        )

        val existingUser = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("existing@example.com"),
            displayName = "Existing User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        whenever(userRepository.findByEmail(any())).thenReturn(existingUser)

        val exception = assertThrows<IllegalArgumentException> {
            authenticationService.register(request)
        }

        assertTrue(exception.message!!.contains("already registered"))
    }

    @Test
    fun `should login successfully with correct credentials`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "SecurePass123"
        )

        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("test@example.com"),
            displayName = "Test User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed-password"
        )

        whenever(userRepository.findByEmail(any())).thenReturn(user)
        whenever(passwordHashingService.verifyPassword(any(), any())).thenReturn(true)
        whenever(refreshTokenRepository.save(any())).thenAnswer { it.arguments[0] as RefreshToken }

        val response = authenticationService.login(request)

        assertTrue(response.accessToken.isNotBlank())
        assertTrue(response.refreshToken.isNotBlank())
        assertEquals(user.email.value, response.user.email)
        assertEquals(user.displayName, response.user.displayName)
        assertEquals(user.role, response.user.role)

        verify(passwordHashingService).verifyPassword(any(), any())
        verify(refreshTokenRepository).save(any())
    }

    @Test
    fun `should throw exception for invalid email during login`() {
        val request = LoginRequest(
            email = "nonexistent@example.com",
            password = "SecurePass123"
        )

        whenever(userRepository.findByEmail(any())).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            authenticationService.login(request)
        }

        assertEquals("Invalid email or password", exception.message)
    }

    @Test
    fun `should throw exception for incorrect password`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "WrongPassword"
        )

        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("test@example.com"),
            displayName = "Test User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed-password"
        )

        whenever(userRepository.findByEmail(any())).thenReturn(user)
        whenever(passwordHashingService.verifyPassword(any(), any())).thenReturn(false)

        val exception = assertThrows<IllegalArgumentException> {
            authenticationService.login(request)
        }

        assertEquals("Invalid email or password", exception.message)
    }

    @Test
    fun `should refresh access token successfully`() {
        val request = RefreshRequest(refreshToken = "valid-refresh-token")
        
        val userId = UserId(UUID.randomUUID())
        val refreshToken = RefreshToken.create(userId, "valid-refresh-token")
        
        val user = User(
            id = userId,
            email = EmailAddress("test@example.com"),
            displayName = "Test User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        whenever(refreshTokenRepository.findByToken(any())).thenReturn(refreshToken)
        whenever(userRepository.findById(any())).thenReturn(user)

        val response = authenticationService.refresh(request)

        assertTrue(response.accessToken.isNotBlank())
        
        verify(refreshTokenRepository).findByToken(any())
    }

    @Test
    fun `should throw exception for invalid refresh token`() {
        val request = RefreshRequest(refreshToken = "invalid-token")

        whenever(refreshTokenRepository.findByToken(any())).thenReturn(null)

        val exception = assertThrows<IllegalArgumentException> {
            authenticationService.refresh(request)
        }

        assertEquals("Invalid refresh token", exception.message)
    }

    @Test
    fun `should throw exception for expired refresh token`() {
        val request = RefreshRequest(refreshToken = "expired-token")
        
        val userId = UserId(UUID.randomUUID())
        val expiredToken = RefreshToken(
            id = UUID.randomUUID(),
            token = "expired-token",
            userId = userId,
            expiresAt = Instant.now().minusSeconds(3600), // Expired 1 hour ago
            createdAt = Instant.now().minusSeconds(7200)
        )

        whenever(refreshTokenRepository.findByToken(any())).thenReturn(expiredToken)

        val exception = assertThrows<IllegalArgumentException> {
            authenticationService.refresh(request)
        }

        assertEquals("Refresh token expired", exception.message)
        verify(refreshTokenRepository).deleteById(expiredToken.id)
    }

    @Test
    fun `should logout user successfully`() {
        val userId = UserId(UUID.randomUUID())

        val response = authenticationService.logout(userId)

        assertEquals("Logged out successfully", response.message)
        verify(refreshTokenRepository).deleteByUserId(userId)
    }

    @Test
    fun `should get user profile successfully`() {
        val userId = UserId(UUID.randomUUID())
        val user = User(
            id = userId,
            email = EmailAddress("test@example.com"),
            displayName = "Test User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        whenever(userRepository.findById(userId)).thenReturn(user)

        val response = authenticationService.getUserProfile(userId)

        assertEquals(user.id.value, response.id)
        assertEquals(user.email.value, response.email)
        assertEquals(user.displayName, response.displayName)
        assertEquals(user.role, response.role)
    }

    @Test
    fun `should update user profile successfully`() {
        val userId = UserId(UUID.randomUUID())
        val request = UpdateProfileRequest(displayName = "Updated Name")
        
        val user = User(
            id = userId,
            email = EmailAddress("test@example.com"),
            displayName = "Old Name",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        whenever(userRepository.findById(userId)).thenReturn(user)
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val response = authenticationService.updateProfile(userId, request)

        assertEquals("Updated Name", response.displayName)
        verify(userRepository).save(argThat { displayName == "Updated Name" })
    }
}
