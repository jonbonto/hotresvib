package com.hotresvib.domain.hotel

import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId

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

data class Room(
    val id: RoomId,
    val hotelId: HotelId,
    val number: RoomNumber,
    val type: RoomType,
    val baseRate: Money
)
