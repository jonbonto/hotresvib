package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.shared.RoomId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PricingRuleJpaRepository : JpaRepository<PricingRule, String> {
    fun findByRoomId(roomId: RoomId): List<PricingRule>
}
