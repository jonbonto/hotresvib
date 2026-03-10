package com.hotresvib.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hotresvib.application.dto.CheckAvailabilityRequest
import com.hotresvib.application.dto.CheckAvailabilityResponse
import com.hotresvib.application.dto.ReservationRequest
import com.hotresvib.application.dto.ReservationResponse
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReservationPricingConsistencyIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var pricingRuleRepository: PricingRuleRepository

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `check availability price matches created reservation price`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("401"),
                type = RoomType.DOUBLE,
                baseRate = Money.of("USD", BigDecimal("100.00"))
            )
        )

        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("weekday"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)),
                price = Money.of("USD", BigDecimal("125.00")),
                description = null
            )
        )
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("weekend"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 6)),
                price = Money.of("USD", BigDecimal("175.00")),
                description = null
            )
        )

        val availabilityRequest = CheckAvailabilityRequest(
            roomId = roomId.value,
            startDate = LocalDate.of(2026, 8, 2),
            endDate = LocalDate.of(2026, 8, 6)
        )

        val availabilityResponseBody = mockMvc.perform(
            post("/api/reservations/check-availability")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(availabilityRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.available").value(true))
            .andReturn()
            .response
            .contentAsString

        val availabilityResponse = objectMapper.readValue(
            availabilityResponseBody,
            CheckAvailabilityResponse::class.java
        )

        val userId = UUID.randomUUID().toString()
        val authentication = UsernamePasswordAuthenticationToken(
            userId,
            null,
            listOf(SimpleGrantedAuthority("ROLE_CUSTOMER"))
        )
        SecurityContextHolder.getContext().authentication = authentication

        val reservationResponseBody = mockMvc.perform(
            post("/api/reservations")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        ReservationRequest(
                            roomId = roomId.value,
                            checkInDate = availabilityRequest.startDate,
                            checkOutDate = availabilityRequest.endDate
                        )
                    )
                )
        )
            .andExpect(status().isCreated)
            .andReturn()
            .response
            .contentAsString

        val reservationResponse = objectMapper.readValue(
            reservationResponseBody,
            ReservationResponse::class.java
        )

        assertThat(reservationResponse.totalPrice).isEqualTo(availabilityResponse.totalPrice)
        assertThat(reservationResponse.totalPrice).isEqualTo(650.0)
    }
}
