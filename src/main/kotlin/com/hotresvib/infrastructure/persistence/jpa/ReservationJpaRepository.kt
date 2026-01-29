package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationJpaRepository : JpaRepository<Reservation, ReservationId> {
    fun findByUserId(userId: UserId): List<Reservation>
    fun findByStatus(status: ReservationStatus): List<Reservation>
}
