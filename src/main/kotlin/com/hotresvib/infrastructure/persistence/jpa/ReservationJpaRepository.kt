package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReservationJpaRepository : JpaRepository<Reservation, UUID> {
    fun findByUserId(userId: UserId): List<Reservation>
    fun findByStatus(status: ReservationStatus): List<Reservation>
}
