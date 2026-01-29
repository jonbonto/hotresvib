package com.hotresvib.application.port

import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId

interface ReservationRepository {
    fun findById(id: ReservationId): Reservation?
    fun findByUserId(userId: UserId): List<Reservation>
    fun findByStatus(status: ReservationStatus): List<Reservation>
    fun save(reservation: Reservation): Reservation
}
