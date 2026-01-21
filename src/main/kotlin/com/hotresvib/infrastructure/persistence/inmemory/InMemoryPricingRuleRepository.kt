package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

class InMemoryPricingRuleRepository : PricingRuleRepository {
    private val storage = ConcurrentHashMap<PricingRuleId, PricingRule>()

    override fun findById(id: PricingRuleId): PricingRule? = storage[id]

    override fun findByRoomId(roomId: RoomId): List<PricingRule> =
        storage.values.filter { it.roomId == roomId }

    override fun save(rule: PricingRule): PricingRule {
        storage[rule.id] = rule
        return rule
    }
}
