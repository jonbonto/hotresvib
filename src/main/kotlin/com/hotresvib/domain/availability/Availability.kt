package com.hotresvib.domain.availability

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import java.util.UUID
import jakarta.persistence.*

@JvmInline
value class AvailabilityId(val value: UUID) {
    companion object {
        fun generate(): AvailabilityId = AvailabilityId(UUID.randomUUID())
    }
}

@JvmInline
value class BlockoutReason(val value: String) {
    init {
        require(value.isNotBlank()) { "Blockout reason must not be blank" }
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

    @Column(name = "reason", nullable = false)
    val reason: BlockoutReason,
    
    // Phase 11: Optimistic locking
    @Version
    @Column(name = "version")
    val version: Long? = null
)

