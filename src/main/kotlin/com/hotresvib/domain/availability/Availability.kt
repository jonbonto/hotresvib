package com.hotresvib.domain.availability

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import java.util.UUID
import java.time.LocalDate
import jakarta.persistence.*

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

@Entity
@Table(name = "availability")
data class Availability(
    @Id
    val id: AvailabilityId,

    val roomId: RoomId,

    @Embedded
    val range: DateRange,

    @Column(name = "available", nullable = false)
    val available: AvailableQuantity
)

