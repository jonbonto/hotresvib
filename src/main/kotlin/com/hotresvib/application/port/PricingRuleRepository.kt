package com.hotresvib.application.port

import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.shared.RoomId

interface PricingRuleRepository {
    fun findById(id: PricingRuleId): PricingRule?
    fun findByRoomId(roomId: RoomId): List<PricingRule>
    fun save(rule: PricingRule): PricingRule
}
