package com.hotresvib.application.port

import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId

interface ReservationRepository {
    fun findById(id: ReservationId): Reservation?
    fun findByUserId(userId: UserId): List<Reservation>
    fun hasConflict(roomId: RoomId, range: DateRange, activeStatuses: Set<ReservationStatus>): Boolean
    fun findByStatus(status: ReservationStatus): List<Reservation>
    fun save(reservation: Reservation): Reservation
}
