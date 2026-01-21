package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import java.util.concurrent.ConcurrentHashMap

class InMemoryReservationRepository : ReservationRepository {
    private val storage = ConcurrentHashMap<ReservationId, Reservation>()
    private val lock = Any()

    override fun findById(id: ReservationId): Reservation? = storage[id]

    override fun findByUserId(userId: UserId): List<Reservation> =
        synchronized(lock) {
            storage.values.filter { it.userId == userId }
        }

    override fun save(reservation: Reservation): Reservation {
        synchronized(lock) {
            storage[reservation.id] = reservation
        }
        return reservation
    }
}
