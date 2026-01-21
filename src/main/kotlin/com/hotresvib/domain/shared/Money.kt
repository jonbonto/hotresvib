package com.hotresvib.domain.shared

import java.math.BigDecimal
import java.util.Currency

data class Money(
    val amount: BigDecimal,
    val currency: Currency
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount must be non-negative" }
    }

    companion object {
        fun of(currencyCode: String, amount: BigDecimal): Money {
            val currency = Currency.getInstance(currencyCode)
            return Money(amount = amount, currency = currency)
        }
    }
}
