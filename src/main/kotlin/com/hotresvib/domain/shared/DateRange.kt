package com.hotresvib.domain.shared

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class DateRange(val startDate: LocalDate, val endDate: LocalDate) {
    init {
        require(!endDate.isBefore(startDate)) { "End date must be on or after start date" }
    }

    val nights: Int = ChronoUnit.DAYS.between(startDate, endDate).toInt()

    /**
     * Treat ranges as half-open intervals [startDate, endDate) when checking overlaps.
     */
    fun overlaps(other: DateRange): Boolean {
        return startDate.isBefore(other.endDate) && other.startDate.isBefore(endDate)
    }
}
