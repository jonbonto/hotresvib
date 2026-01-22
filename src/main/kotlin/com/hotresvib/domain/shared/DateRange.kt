package com.hotresvib.domain.shared

import java.time.LocalDate

data class DateRange(val start: LocalDate, val end: LocalDate) {
    init {
        require(!end.isBefore(start)) { "End date must be on or after start date" }
    }

    /**
     * Treats ranges as half-open intervals [start, end) to align with availability constraints.
     */
    fun overlapsHalfOpen(other: DateRange): Boolean {
        return start.isBefore(other.end) && other.start.isBefore(end)
    }
}
