package com.hotresvib.application.service

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
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryPricingRuleRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryRoomRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class PriceCalculationServiceTest {

    private val roomRepository: RoomRepository = InMemoryRoomRepository()
    private val pricingRuleRepository: PricingRuleRepository = InMemoryPricingRuleRepository()
    private val service = PriceCalculationService(roomRepository, pricingRuleRepository)

    @Test
    fun `calculates per-night total for a stay straddling two pricing rules`() {
        val roomId = RoomId.generate()
        roomRepository.save(
            Room(
                id = roomId,
                hotelId = HotelId.generate(),
                number = RoomNumber("301"),
                type = RoomType.SUITE,
                baseRate = Money.of("USD", BigDecimal("100.00"))
            )
        )

        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("rule-one"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 4)),
                price = Money.of("USD", BigDecimal("120.00")),
                description = "Early March"
            )
        )
        pricingRuleRepository.save(
            PricingRule(
                id = PricingRuleId("rule-two"),
                roomId = roomId,
                range = DateRange(LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 7)),
                price = Money.of("USD", BigDecimal("150.00")),
                description = "Peak weekend"
            )
        )

        val total = service.calculateTotalPrice(
            roomId,
            LocalDate.of(2024, 3, 2),
            LocalDate.of(2024, 3, 8)
        )

        assertThat(total.amount).isEqualByComparingTo(BigDecimal("790.00"))
        assertThat(total.currency).isEqualTo("USD")
    }
}
