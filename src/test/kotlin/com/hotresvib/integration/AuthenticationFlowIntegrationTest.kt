package com.hotresvib.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hotresvib.application.dto.LoginRequest
import com.hotresvib.application.dto.RefreshRequest
import com.hotresvib.application.dto.RegisterRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationFlowIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `complete authentication flow - register, login, access protected resource, refresh token, logout`() {
        val uniqueEmail = "integrationtest${System.currentTimeMillis()}@example.com"
        
        // 1. Register new user
        val registerRequest = RegisterRequest(
            email = uniqueEmail,
            password = "SecurePass123",
            displayName = "Integration Test User"
        )

        val registerResult = mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.message").value("Registration successful"))
            .andExpect(jsonPath("$.user.email").value(uniqueEmail))
            .andReturn()

        // 2. Login with registered credentials
        val loginRequest = LoginRequest(
            email = uniqueEmail,
            password = "SecurePass123"
        )

        val loginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.user.email").value(uniqueEmail))
            .andReturn()

        val loginResponse = objectMapper.readTree(loginResult.response.contentAsString)
        val accessToken = loginResponse.get("accessToken").asText()
        val refreshToken = loginResponse.get("refreshToken").asText()

        // 3. Access protected resource with access token
        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $accessToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(uniqueEmail))
            .andExpect(jsonPath("$.displayName").value("Integration Test User"))

        // 4. Refresh access token
        val refreshRequest = RefreshRequest(refreshToken = refreshToken)

        val refreshResult = mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andReturn()

        val newAccessToken = objectMapper.readTree(refreshResult.response.contentAsString)
            .get("accessToken").asText()

        // 5. Access protected resource with new access token
        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer $newAccessToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(uniqueEmail))

        // 6. Logout
        mockMvc.perform(
            post("/api/auth/logout")
                .header("Authorization", "Bearer $newAccessToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Logged out successfully"))

        // 7. Verify refresh token no longer works after logout
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should deny access to protected resources without authentication`() {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized)

        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `should reject invalid JWT token`() {
        mockMvc.perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer invalid-token-12345")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `role-based access control - CUSTOMER cannot create hotels`() {
        val uniqueEmail = "customer${System.currentTimeMillis()}@example.com"
        
        // Register and login as CUSTOMER
        val registerRequest = RegisterRequest(
            email = uniqueEmail,
            password = "SecurePass123",
            displayName = "Customer User"
        )

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)

        val loginRequest = LoginRequest(
            email = uniqueEmail,
            password = "SecurePass123"
        )

        val loginResult = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andReturn()

        val accessToken = objectMapper.readTree(loginResult.response.contentAsString)
            .get("accessToken").asText()

        // Attempt to create hotel (requires ADMIN role)
        val hotelRequest = mapOf(
            "name" to "Test Hotel",
            "description" to "Test Description",
            "address" to "123 Test St",
            "city" to "Test City",
            "country" to "Test Country",
            "starRating" to 5
        )

        mockMvc.perform(
            post("/api/hotels")
                .header("Authorization", "Bearer $accessToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hotelRequest))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should prevent duplicate user registration`() {
        val uniqueEmail = "duplicate${System.currentTimeMillis()}@example.com"
        
        val registerRequest = RegisterRequest(
            email = uniqueEmail,
            password = "SecurePass123",
            displayName = "Test User"
        )

        // First registration succeeds
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isCreated)

        // Second registration with same email fails
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest))
        )
            .andExpect(status().isConflict)
    }
}
