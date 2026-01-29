package com.hotresvib.application.service

import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.shared.RoomId
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PricingApplicationService(
    private val pricingRuleRepository: PricingRuleRepository
) {
    fun getApplicablePrice(roomId: RoomId, date: LocalDate): PricingRule? {
        val rules = pricingRuleRepository.findByRoomId(roomId)
        // return first rule whose range contains the date
        return rules.firstOrNull { !date.isBefore(it.range.startDate) && date.isBefore(it.range.endDate) }
    }
}
