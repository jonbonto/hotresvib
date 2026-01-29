package com.hotresvib.domain.shared

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDate

class DateRangeTest {

    @Test
    fun `should create valid date range`() {
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)
        val dateRange = DateRange(startDate, endDate)

        assertThat(dateRange.startDate).isEqualTo(startDate)
        assertThat(dateRange.endDate).isEqualTo(endDate)
    }

    @Test
    fun `should fail when end date is before start date`() {
        val startDate = LocalDate.of(2026, 2, 5)
        val endDate = LocalDate.of(2026, 2, 1)

        assertThrows<IllegalArgumentException> {
            DateRange(startDate, endDate)
        }
    }

    @Test
    fun `should calculate number of nights correctly`() {
        val startDate = LocalDate.of(2026, 2, 1)
        val endDate = LocalDate.of(2026, 2, 5)
        val dateRange = DateRange(startDate, endDate)

        assertThat(dateRange.nights).isEqualTo(4)
    }

    @Test
    fun `should check if dates overlap`() {
        val range1 = DateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 5))
        val range2 = DateRange(LocalDate.of(2026, 2, 3), LocalDate.of(2026, 2, 7))
        val range3 = DateRange(LocalDate.of(2026, 2, 6), LocalDate.of(2026, 2, 10))

        assertThat(range1.overlaps(range2)).isTrue()
        assertThat(range1.overlaps(range3)).isFalse()
    }
}
