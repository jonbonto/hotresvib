package com.hotresvib.domain.shared

import java.time.LocalDate

data class DateRange(val start: LocalDate, val end: LocalDate) {
    init {
        require(!end.isBefore(start)) { "End date must be on or after start date" }
    }
}
