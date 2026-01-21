package com.hotresvib.domain.availability

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId

@JvmInline
value class AvailableQuantity(val value: Int) {
    init {
        require(value >= 0) { "Available quantity must be non-negative" }
    }
}

data class Availability(
    val roomId: RoomId,
    val range: DateRange,
    val available: AvailableQuantity
)
