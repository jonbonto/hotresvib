package com.hotresvib.domain.shared

import java.math.BigDecimal

@JvmInline
value class Money(val amount: BigDecimal) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount must be non-negative" }
    }
}
