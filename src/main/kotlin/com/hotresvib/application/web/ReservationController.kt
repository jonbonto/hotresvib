package com.hotresvib.application.web

import com.hotresvib.application.service.ReservationApplicationService
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.shared.ReservationId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val reservationApplicationService: ReservationApplicationService
) {
    @PostMapping
    fun createReservation(@RequestBody reservation: Reservation): ResponseEntity<Reservation> {
        val newReservation = reservationApplicationService.createReservation(reservation)
        return ResponseEntity.ok(newReservation)
    }

    @GetMapping("/{id}")
    fun getReservation(@PathVariable id: UUID): ResponseEntity<Reservation> {
        val reservation = reservationApplicationService.findById(ReservationId(id))
        return reservation?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }
}
