package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.persistence.jpa.ReservationJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class ReservationJpaAdapter(private val repo: ReservationJpaRepository) : ReservationRepository {
    override fun findById(id: ReservationId): Reservation? = repo.findById(id).orElse(null)

    override fun findByUserId(userId: UserId): List<Reservation> = repo.findByUserId(userId)

    override fun save(reservation: Reservation): Reservation = repo.save(reservation)
}
