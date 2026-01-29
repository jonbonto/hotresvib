package com.hotresvib.infrastructure.persistence.inmemory

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.shared.RoomId
import java.util.concurrent.ConcurrentHashMap

@Repository
@Profile("demo")
class InMemoryPricingRuleRepository : PricingRuleRepository {
    private val storage = ConcurrentHashMap<PricingRuleId, PricingRule>()
    private val lock = Any()

    override fun findById(id: PricingRuleId): PricingRule? =
        synchronized(lock) {
            storage[id]
        }

    override fun findByRoomId(roomId: RoomId): List<PricingRule> =
        synchronized(lock) {
            storage.values.filter { it.roomId == roomId }
        }

    override fun save(rule: PricingRule): PricingRule {
        synchronized(lock) {
            storage[rule.id] = rule
        }
        return rule
    }
}
