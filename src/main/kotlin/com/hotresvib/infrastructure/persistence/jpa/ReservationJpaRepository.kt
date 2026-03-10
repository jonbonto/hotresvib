package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface ReservationJpaRepository : JpaRepository<Reservation, UUID> {
    fun findByUserId(userId: UserId): List<Reservation>
    @Query(
        """
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.roomId = :roomId
          AND r.status IN :activeStatuses
          AND r.stay.startDate < :endDate
          AND r.stay.endDate > :startDate
        """
    )
    fun existsConflict(
        roomId: RoomId,
        startDate: LocalDate,
        endDate: LocalDate,
        activeStatuses: Set<ReservationStatus>
    ): Boolean
    fun findByStatus(status: ReservationStatus): List<Reservation>
}
