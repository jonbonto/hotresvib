package com.hotresvib.domain.shared

import java.math.BigDecimal
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Money(
    @Column(name = "amount", nullable = false)
    val amount: BigDecimal,

    @Column(name = "currency", nullable = false, length = 3)
    val currency: String
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Amount must be non-negative" }
        require(currency.isNotBlank()) { "Currency must not be blank" }
    }

    fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add money with different currencies" }
        return Money(amount + other.amount, currency)
    }

    fun times(multiplier: Int): Money {
        require(multiplier >= 0) { "Multiplier must be non-negative" }
        return Money(amount.multiply(BigDecimal.valueOf(multiplier.toLong())), currency)
    }

    companion object {
        fun of(currencyCode: String, amount: BigDecimal): Money = Money(amount, currencyCode)
    }
}
