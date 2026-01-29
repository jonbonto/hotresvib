package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.service.AuthenticationService
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.UserRole
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for unit tests
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    @Test
    fun `POST register should return 201 Created on successful registration`() {
        val request = RegisterRequest(
            email = "newuser@example.com",
            password = "SecurePass123",
            displayName = "New User"
        )

        val response = RegisterResponse(
            message = "Registration successful",
            user = UserResponse(
                id = UUID.randomUUID(),
                email = "newuser@example.com",
                displayName = "New User",
                role = UserRole.CUSTOMER
            )
        )

        whenever(authenticationService.register(any())).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").value("Registration successful"))
            .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.user.displayName").value("New User"))
            .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
    }

    @Test
    fun `POST register should return 409 Conflict for duplicate email`() {
        val request = RegisterRequest(
            email = "existing@example.com",
            password = "SecurePass123",
            displayName = "New User"
        )

        whenever(authenticationService.register(any()))
            .thenThrow(IllegalArgumentException("Email already registered"))

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Email already registered"))
    }

    @Test
    fun `POST register should return 400 Bad Request for invalid email format`() {
        val request = mapOf(
            "email" to "invalid-email",
            "password" to "SecurePass123",
            "displayName" to "New User"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST register should return 400 Bad Request for short password`() {
        val request = mapOf(
            "email" to "test@example.com",
            "password" to "short",
            "displayName" to "New User"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST login should return 200 OK with tokens on successful authentication`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "SecurePass123"
        )

        val userId = UUID.randomUUID()
        val response = AuthResponse(
            accessToken = "jwt-access-token",
            refreshToken = "refresh-token-uuid",
            user = UserResponse(
                id = userId,
                email = "test@example.com",
                displayName = "Test User",
                role = UserRole.CUSTOMER
            )
        )

        whenever(authenticationService.login(any())).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token-uuid"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"))
            .andExpect(jsonPath("$.user.displayName").value("Test User"))
    }

    @Test
    fun `POST login should return 401 Unauthorized for invalid credentials`() {
        val request = LoginRequest(
            email = "test@example.com",
            password = "WrongPassword"
        )

        whenever(authenticationService.login(any()))
            .thenThrow(IllegalArgumentException("Invalid email or password"))

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid email or password"))
    }

    @Test
    fun `POST refresh should return 200 OK with new access token`() {
        val request = RefreshRequest(refreshToken = "valid-refresh-token")

        val response = RefreshResponse(accessToken = "new-access-token")

        whenever(authenticationService.refresh(any())).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
    }

    @Test
    fun `POST refresh should return 401 Unauthorized for invalid refresh token`() {
        val request = RefreshRequest(refreshToken = "invalid-token")

        whenever(authenticationService.refresh(any()))
            .thenThrow(IllegalArgumentException("Invalid refresh token"))

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid refresh token"))
    }

    @Test
    fun `POST logout should return 200 OK`() {
        val userId = UUID.randomUUID()
        val response = LogoutResponse(message = "Logged out successfully")

        whenever(authenticationService.logout(any())).thenReturn(response)

        mockMvc.perform(
            post("/api/auth/logout")
                .requestAttr("userId", UserId(userId))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Logged out successfully"))
    }

    @Test
    fun `GET me should return 200 OK with user profile`() {
        val userId = UUID.randomUUID()
        val response = UserResponse(
            id = userId,
            email = "test@example.com",
            displayName = "Test User",
            role = UserRole.CUSTOMER
        )

        whenever(authenticationService.getUserProfile(any())).thenReturn(response)

        mockMvc.perform(
            get("/api/auth/me")
                .requestAttr("userId", UserId(userId))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.displayName").value("Test User"))
            .andExpect(jsonPath("$.role").value("CUSTOMER"))
    }

    @Test
    fun `GET me should return 401 Unauthorized without authentication`() {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `PUT me should return 200 OK with updated profile`() {
        val userId = UUID.randomUUID()
        val request = UpdateProfileRequest(displayName = "Updated Name")
        
        val response = UserResponse(
            id = userId,
            email = "test@example.com",
            displayName = "Updated Name",
            role = UserRole.CUSTOMER
        )

        whenever(authenticationService.updateProfile(any(), any())).thenReturn(response)

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", UserId(userId))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.displayName").value("Updated Name"))
    }

    @Test
    fun `PUT me should return 400 Bad Request for blank display name`() {
        val request = mapOf("displayName" to "")

        mockMvc.perform(
            put("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .requestAttr("userId", UserId(UUID.randomUUID()))
        )
            .andExpect(status().isBadRequest)
    }
}
