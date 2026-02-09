package com.hotresvib.application.notification

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

class EmailTemplateServiceTest {

    private lateinit var mockTemplateEngine: TemplateEngine
    private lateinit var emailTemplateService: EmailTemplateService

    @BeforeEach
    fun setup() {
        mockTemplateEngine = mock()
        emailTemplateService = EmailTemplateService(mockTemplateEngine)
    }

    @Test
    fun `renderTemplate should return rendered HTML content`() {
        // Arrange
        val templateName = "email/booking-confirmation"
        val variables = mapOf(
            "guestName" to "John Doe",
            "hotelName" to "Grand Plaza Hotel"
        )
        val expectedHtml = "<html>Booking confirmed</html>"
        
        whenever(mockTemplateEngine.process(templateName, org.mockito.kotlin.any())).thenReturn(expectedHtml)

        // Act
        val result = emailTemplateService.renderTemplate(templateName, variables)

        // Assert
        assertEquals(expectedHtml, result)
    }

    @Test
    fun `renderTemplateWithDefaults should add default values to context`() {
        // Arrange
        val templateName = "email/check-in-reminder"
        val variables = mapOf(
            "guestName" to "Jane Smith",
            "hotelName" to "Beach Resort"
        )
        val expectedHtml = "<html>Check-in reminder</html>"
        
        whenever(mockTemplateEngine.process(templateName, org.mockito.kotlin.any())).thenReturn(expectedHtml)

        // Act
        val result = emailTemplateService.renderTemplateWithDefaults(templateName, variables)

        // Assert
        assertEquals(expectedHtml, result)
    }

    @Test
    fun `renderTemplate should throw exception for invalid template`() {
        // Arrange
        val templateName = "email/invalid"
        val variables = emptyMap<String, Any>()
        
        whenever(mockTemplateEngine.process(templateName, org.mockito.kotlin.any()))
            .thenThrow(RuntimeException("Template not found"))

        // Act & Assert
        assertThrows(IllegalArgumentException::class.java) {
            emailTemplateService.renderTemplate(templateName, variables)
        }
    }
}
