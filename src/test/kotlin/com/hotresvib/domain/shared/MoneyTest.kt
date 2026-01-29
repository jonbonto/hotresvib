package com.hotresvib.domain.shared

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal

class MoneyTest {

    @Test
    fun `should create valid money`() {
        val money = Money(BigDecimal("100.00"), "USD")

        assertThat(money.amount).isEqualTo(BigDecimal("100.00"))
        assertThat(money.currency).isEqualTo("USD")
    }

    @Test
    fun `should fail when amount is negative`() {
        assertThrows<IllegalArgumentException> {
            Money(BigDecimal("-100.00"), "USD")
        }
    }

    @Test
    fun `should add money with same currency`() {
        val money1 = Money(BigDecimal("100.00"), "USD")
        val money2 = Money(BigDecimal("50.00"), "USD")

        val result = money1.plus(money2)

        assertThat(result.amount).isEqualTo(BigDecimal("150.00"))
        assertThat(result.currency).isEqualTo("USD")
    }

    @Test
    fun `should fail when adding money with different currencies`() {
        val money1 = Money(BigDecimal("100.00"), "USD")
        val money2 = Money(BigDecimal("50.00"), "EUR")

        assertThrows<IllegalArgumentException> {
            money1.plus(money2)
        }
    }

    @Test
    fun `should multiply money`() {
        val money = Money(BigDecimal("100.00"), "USD")

        val result = money.times(3)

        assertThat(result.amount).isEqualTo(BigDecimal("300.00"))
        assertThat(result.currency).isEqualTo("USD")
    }
}
