package com.hotresvib.domain.pricing

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId

@JvmInline
value class PricingRuleId(val value: String) {
    init {
        require(value.isNotBlank()) { "Pricing rule id is required" }
    }
}

data class PricingRule(
    val id: PricingRuleId,
    val roomId: RoomId,
    val range: DateRange,
    val price: Money,
    val description: String?
)
