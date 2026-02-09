package com.hotresvib.application.notification

import com.hotresvib.application.port.EmailUnsubscribeRepository
import com.hotresvib.domain.notification.EmailUnsubscribe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any

class EmailPreferenceServiceTest {

    private lateinit var mockRepository: EmailUnsubscribeRepository
    private lateinit var emailPreferenceService: EmailPreferenceService

    @BeforeEach
    fun setup() {
        mockRepository = mock()
        emailPreferenceService = EmailPreferenceService(mockRepository)
    }

    @Test
    fun `isUnsubscribed should return true for unsubscribed email`() {
        // Arrange
        val email = "test@example.com"
        val unsubscribe = EmailUnsubscribe.fromEmail(email, "Not interested", "ALL")
        whenever(mockRepository.findByEmail(email)).thenReturn(unsubscribe)

        // Act
        val result = emailPreferenceService.isUnsubscribed(email)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `isUnsubscribed should return false for subscribed email`() {
        // Arrange
        val email = "test@example.com"
        whenever(mockRepository.findByEmail(email)).thenReturn(null)

        // Act
        val result = emailPreferenceService.isUnsubscribed(email)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `unsubscribe should create unsubscribe record`() {
        // Arrange
        val email = "test@example.com"
        whenever(mockRepository.findByEmail(email)).thenReturn(null)
        whenever(mockRepository.save(any())).thenReturn(EmailUnsubscribe.fromEmail(email))

        // Act
        val result = emailPreferenceService.unsubscribe(email, "No longer interested")

        // Assert
        assertTrue(result)
        verify(mockRepository).save(any())
    }

    @Test
    fun `resubscribe should delete unsubscribe record`() {
        // Arrange
        val email = "test@example.com"
        val unsubscribe = EmailUnsubscribe.fromEmail(email)
        whenever(mockRepository.findByEmail(email)).thenReturn(unsubscribe)

        // Act
        val result = emailPreferenceService.resubscribe(email)

        // Assert
        assertTrue(result)
        verify(mockRepository).delete(unsubscribe)
    }

    @Test
    fun `canSendMarketing should return false if unsubscribed from promotions`() {
        // Arrange
        val email = "test@example.com"
        val unsubscribe = EmailUnsubscribe(
            email = email,
            emailType = "PROMOTIONAL"
        )
        whenever(mockRepository.findByEmail(email)).thenReturn(unsubscribe)

        // Act
        val result = emailPreferenceService.canSendMarketing(email)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `canSendTransactional should return true unless completely unsubscribed`() {
        // Arrange
        val email = "test@example.com"
        val unsubscribe = EmailUnsubscribe(
            email = email,
            emailType = "PROMOTIONAL"
        )
        whenever(mockRepository.findByEmail(email)).thenReturn(unsubscribe)

        // Act
        val result = emailPreferenceService.canSendTransactional(email)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `canSendTransactional should return false if completely unsubscribed`() {
        // Arrange
        val email = "test@example.com"
        val unsubscribe = EmailUnsubscribe(
            email = email,
            emailType = "ALL"
        )
        whenever(mockRepository.findByEmail(email)).thenReturn(unsubscribe)

        // Act
        val result = emailPreferenceService.canSendTransactional(email)

        // Assert
        assertFalse(result)
    }
}
