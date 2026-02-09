package com.hotresvib.application.web

import com.hotresvib.application.notification.EmailPreferenceService
import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class EmailPreferenceControllerTest {

    private lateinit var mockService: EmailPreferenceService
    private lateinit var mockUserRepo: UserRepository
    private lateinit var controller: EmailPreferenceController

    @BeforeEach
    fun setup() {
        mockService = mock()
        mockUserRepo = mock()
        controller = EmailPreferenceController(mockService, mockUserRepo)
    }

    @Test
    fun `unsubscribeByToken should return 200 and update user when token valid`() {
        val token = "token-123"
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("user@test.com"),
            displayName = "Test User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        ).withUnsubscribeToken(token)

        whenever(mockUserRepo.findByUnsubscribeToken(token)).thenReturn(user)
        whenever(mockService.unsubscribe(user.email.value, "Unsubscribed via link", "ALL")).thenReturn(true)
        whenever(mockUserRepo.save(any())).thenReturn(user.withMarketingOptOut())

        val response = controller.unsubscribeByToken(token)

        assertEquals(200, response.statusCodeValue)
        assertEquals("success", response.body?.get("status"))
        verify(mockService, times(1)).unsubscribe(user.email.value, "Unsubscribed via link", "ALL")
        verify(mockUserRepo, times(1)).save(any())
    }

    @Test
    fun `unsubscribeByToken should return 404 when token invalid`() {
        val token = "non-existent"
        whenever(mockUserRepo.findByUnsubscribeToken(token)).thenReturn(null)

        val response = controller.unsubscribeByToken(token)

        assertEquals(404, response.statusCodeValue)
        assertEquals("error", response.body?.get("status"))
    }
}
