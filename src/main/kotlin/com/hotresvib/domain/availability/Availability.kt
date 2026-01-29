package com.hotresvib.domain.availability

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import java.util.UUID
import java.time.LocalDate

@JvmInline
value class AvailabilityId(val value: UUID) {
    companion object {
        fun generate(): AvailabilityId = AvailabilityId(UUID.randomUUID())
    }
}

@JvmInline
value class AvailableQuantity(val value: Int) {
    init {
        require(value >= 0) { "Available quantity must be non-negative" }
    }
}

data class Availability(
    val id: AvailabilityId,
    val roomId: RoomId,
    val range: DateRange,
    val available: AvailableQuantity
)

{
    constructor(id: UUID, roomId: UUID, start: LocalDate, end: LocalDate, available: Int) : this(
        AvailabilityId(id),
        RoomId(roomId),
        DateRange(start, end),
        AvailableQuantity(available)
    )
}
