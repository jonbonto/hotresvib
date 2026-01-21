package com.hotresvib.domain.shared

import java.util.UUID

@JvmInline
value class HotelId(val value: UUID)

@JvmInline
value class RoomId(val value: UUID)

@JvmInline
value class UserId(val value: UUID)

@JvmInline
value class ReservationId(val value: UUID)
