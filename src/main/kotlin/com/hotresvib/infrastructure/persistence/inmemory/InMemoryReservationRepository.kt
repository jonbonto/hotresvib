package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import java.util.concurrent.ConcurrentHashMap

class InMemoryReservationRepository : ReservationRepository {
    private val storage = ConcurrentHashMap<ReservationId, Reservation>()
    private val lock = Any()

    override fun findById(id: ReservationId): Reservation? =
        synchronized(lock) {
            storage[id]
        }

    override fun findByUserId(userId: UserId): List<Reservation> =
        synchronized(lock) {
            storage.values.filter { it.userId == userId }
        }

    override fun hasConflict(roomId: RoomId, range: DateRange, activeStatuses: Set<ReservationStatus>): Boolean =
        synchronized(lock) {
            storage.values.any {
                it.roomId == roomId && it.status in activeStatuses && it.stay.overlaps(range)
            }
        }
    
    override fun findByStatus(status: ReservationStatus): List<Reservation> =
        synchronized(lock) {
            storage.values.filter { it.status == status }
        }

    override fun save(reservation: Reservation): Reservation {
        synchronized(lock) {
            storage[reservation.id] = reservation
        }
        return reservation
    }
}
