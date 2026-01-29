package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.pricing.PricingRuleId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.infrastructure.persistence.jpa.PricingRuleJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class PricingRuleJpaAdapter(
    private val jpaRepository: PricingRuleJpaRepository
) : PricingRuleRepository {
    
    override fun save(pricingRule: PricingRule): PricingRule {
        return jpaRepository.save(pricingRule)
    }
    
    override fun findById(id: PricingRuleId): PricingRule? {
        return jpaRepository.findById(id).orElse(null)
    }
    
    override fun findByRoomId(roomId: RoomId): List<PricingRule> {
        return jpaRepository.findByRoomId(roomId)
    }
}
