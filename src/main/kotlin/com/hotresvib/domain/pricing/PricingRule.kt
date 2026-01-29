package com.hotresvib.domain.pricing

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import jakarta.persistence.*

@JvmInline
value class PricingRuleId(val value: String) {
    init {
        require(value.isNotBlank()) { "Pricing rule id is required" }
    }
}

@Entity
@Table(name = "pricing_rules")
data class PricingRule(
    @Id
    val id: PricingRuleId,

    val roomId: RoomId,

    @Embedded
    val range: DateRange,

    @Embedded
    val price: Money,

    @Column(name = "description")
    val description: String?
)
