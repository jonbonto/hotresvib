package com.hotresvib.application.service

import com.hotresvib.application.port.ReservationRepository
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.shared.ReservationId
import org.springframework.stereotype.Service

@Service
class ReservationApplicationService(
    private val reservationRepository: ReservationRepository
) {
    fun createReservation(reservation: Reservation): Reservation {
        // Basic validation, more complex logic for availability and pricing will be added later
        return reservationRepository.save(reservation)
    }

    fun findById(id: ReservationId): Reservation? {
        return reservationRepository.findById(id)
    }
}
