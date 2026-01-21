package com.hotresvib.domain.shared

import java.util.UUID

@JvmInline
value class HotelId(val value: UUID) {
    companion object {
        fun generate(): HotelId = HotelId(UUID.randomUUID())
    }
}

@JvmInline
value class RoomId(val value: UUID) {
    companion object {
        fun generate(): RoomId = RoomId(UUID.randomUUID())
    }
}

@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
    }
}

@JvmInline
value class ReservationId(val value: UUID) {
    companion object {
        fun generate(): ReservationId = ReservationId(UUID.randomUUID())
    }
}
