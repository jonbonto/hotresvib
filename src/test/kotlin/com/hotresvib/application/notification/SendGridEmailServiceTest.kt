package com.hotresvib.application.notification

import com.hotresvib.application.port.EmailLogRepository
import com.sendgrid.Response
import com.sendgrid.SendGrid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SendGridEmailServiceTest {

    private lateinit var mockLogRepo: EmailLogRepository

    @BeforeEach
    fun setup() {
        mockLogRepo = mock()
    }

    @Test
    fun `sendEmail returns true and logs when SendGrid not configured`() {
        val service = SendGridEmailService(
            sendGridApiKey = "",
            fromEmail = "noreply@test.com",
            fromName = "Test",
            emailLogRepository = mockLogRepo,
            sendGridClient = null
        )

        val request = EmailRequest(
            toEmail = "user@test.com",
            subject = "Hello",
            htmlContent = "<p>hi</p>"
        )

        val result = service.sendEmail(request)

        assertTrue(result)
        verify(mockLogRepo, times(1)).save(any())
    }

    @Test
    fun `sendEmail returns false and logs when SendGrid returns error`() {
        val mockSendGrid: SendGrid = mock()
        val response = Response()
        response.statusCode = 500
        response.body = "Internal Error"
        whenever(mockSendGrid.api(any())).thenReturn(response)

        val service = SendGridEmailService(
            sendGridApiKey = "dummy",
            fromEmail = "noreply@test.com",
            fromName = "Test",
            emailLogRepository = mockLogRepo,
            sendGridClient = mockSendGrid
        )

        val request = EmailRequest(
            toEmail = "user@test.com",
            subject = "Hello",
            htmlContent = "<p>hi</p>"
        )

        val result = service.sendEmail(request)

        assertFalse(result)
        verify(mockLogRepo, times(1)).save(any())
    }
}
