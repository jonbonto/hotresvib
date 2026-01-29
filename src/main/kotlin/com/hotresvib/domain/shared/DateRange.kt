package com.hotresvib.domain.shared

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import jakarta.persistence.Column
import jakarta.persistence.Transient
import jakarta.persistence.Embeddable

@Embeddable
data class DateRange(
    @Column(name = "start_date", nullable = false)
    val startDate: LocalDate,

    @Column(name = "end_date", nullable = false)
    val endDate: LocalDate
) {
    init {
        require(!endDate.isBefore(startDate)) { "End date must be on or after start date" }
    }

    @Transient
    val nights: Int = ChronoUnit.DAYS.between(startDate, endDate).toInt()

    /**
     * Treat ranges as half-open intervals [startDate, endDate) when checking overlaps.
     */
    fun overlaps(other: DateRange): Boolean {
        return startDate.isBefore(other.endDate) && other.startDate.isBefore(endDate)
    }
}
