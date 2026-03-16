package com.hotresvib.domain.hotel

import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import jakarta.persistence.*

@JvmInline
value class RoomNumber(val value: String) {
    init {
        require(value.isNotBlank()) { "Room number is required" }
    }
}

enum class RoomType {
    SINGLE,
    DOUBLE,
    SUITE
}

@Entity
@Table(name = "rooms")
data class Room(
    @Id
    val id: RoomId,

    val hotelId: HotelId,

    @Column(name = "number", nullable = false)
    val number: RoomNumber,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: RoomType,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "amount", column = Column(name = "base_rate_amount", nullable = false)),
        AttributeOverride(name = "currency", column = Column(name = "base_rate_currency", nullable = false, length = 3))
    )
    val baseRate: Money,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "capacity")
    val capacity: Int = 2,

    @Column(name = "amenities", columnDefinition = "TEXT")
    val amenities: String? = null,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null
)
