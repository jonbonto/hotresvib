package com.hotresvib.application.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.hotresvib.application.dto.PaymentRequest
import com.hotresvib.application.dto.PaymentResponse
import com.hotresvib.application.payment.PaymentServiceImpl
import com.hotresvib.application.port.PaymentRepository
import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.application.service.ReservationApplicationService
import com.hotresvib.application.service.StripePaymentService
import com.hotresvib.domain.payment.Payment
import com.hotresvib.domain.payment.PaymentStatus
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var reservationRepository: ReservationRepository

    @MockBean
    private lateinit var paymentService: PaymentServiceImpl

    @MockBean
    private lateinit var paymentRepository: PaymentRepository

    @MockBean
    private lateinit var stripePaymentService: StripePaymentService

    @MockBean
    private lateinit var reservationService: ReservationApplicationService

    private fun makeDummyReservation(userId: UserId): Reservation {
        return Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = userId,
            roomId = RoomId(UUID.randomUUID()),
            stay = DateRange(LocalDate.now(), LocalDate.now().plusDays(1)),
            totalAmount = Money(BigDecimal.ZERO, "USD"),
            status = ReservationStatus.PENDING_PAYMENT,
            createdAt = Instant.now()
        )
    }

    private fun makeDummyPayment(reservationId: ReservationId): Payment {
        return Payment(
            id = UUID.randomUUID(),
            reservationId = reservationId,
            amount = Money(BigDecimal.valueOf(100.0), "USD"),
            status = PaymentStatus.PENDING,
            paymentMethod = "card",
            transactionId = "tx123",
            paymentIntentId = null,
            metadata = null,
            idempotencyKey = null,
            createdAt = Instant.now()
        )
    }

    @Test
    fun `POST payments should return 401 Unauthorized when no authentication provided`() {
        val request = PaymentRequest(
            reservationId = UUID.randomUUID(),
            amount = 100.0,
            currency = "USD",
            paymentMethod = "card"
        )

        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST payments should return 404 Not Found when reservation missing`() {
        val requestId = UUID.randomUUID()
        whenever(reservationRepository.findById(ReservationId(requestId))).thenReturn(null)

        val auth = UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), null)
        val request = PaymentRequest(
            reservationId = requestId,
            amount = 50.0,
            currency = "USD",
            paymentMethod = "card"
        )

        mockMvc.perform(
            post("/api/payments")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `POST payments should return 403 Forbidden for wrong user`() {
        val ownerId = UserId(UUID.randomUUID())
        val reservation = makeDummyReservation(ownerId)
        whenever(reservationRepository.findById(reservation.id)).thenReturn(reservation)

        val otherAuth = UsernamePasswordAuthenticationToken(UUID.randomUUID().toString(), null)
        val request = PaymentRequest(
            reservationId = reservation.id.value,
            amount = 20.0,
            currency = "USD",
            paymentMethod = "card"
        )

        mockMvc.perform(
            post("/api/payments")
                .principal(otherAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `POST payments should return 201 Created on success`() {
        val userUUID = UUID.randomUUID()
        val ownerId = UserId(userUUID)
        val reservation = makeDummyReservation(ownerId)
        whenever(reservationRepository.findById(reservation.id)).thenReturn(reservation)

        val payment = makeDummyPayment(reservation.id)
        // stub with exact values to avoid matcher issues
        whenever(paymentService.processPayment(
            reservation.id,
            Money(BigDecimal.valueOf(20.0), "USD"),
            "card"
        )).thenReturn(payment)

        val auth = UsernamePasswordAuthenticationToken(userUUID.toString(), null)
        val request = PaymentRequest(
            reservationId = reservation.id.value,
            amount = 20.0,
            currency = "USD",
            paymentMethod = "card"
        )

        val mvcResult = mockMvc.perform(
            post("/api/payments")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
            .andReturn()

        // capture status/body for easier debugging
        val status = mvcResult.response.status
        val body = mvcResult.response.contentAsString

        if (status != 201) {
            throw AssertionError("expected 201 Created but was $status; body=$body")
        }

        org.assertj.core.api.Assertions.assertThat(body).contains(reservation.id.value.toString())
        org.assertj.core.api.Assertions.assertThat(body).contains("PENDING")
    }
}
