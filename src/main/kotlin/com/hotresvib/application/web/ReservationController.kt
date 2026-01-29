package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.service.ReservationService
import com.hotresvib.application.service.AvailabilityApplicationService
import com.hotresvib.application.port.*
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.shared.DateRange
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = ["*"])
class ReservationController(
    private val reservationService: ReservationService,
    private val availabilityService: AvailabilityApplicationService,
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository
) {

    @PostMapping("/check-availability")
    fun checkAvailability(@RequestBody request: CheckAvailabilityRequest): ResponseEntity<CheckAvailabilityResponse> {
        return try {
            val available = availabilityService.checkAvailability(
                RoomId(request.roomId),
                request.startDate,
                request.endDate
            )
            val totalPrice = if (available) 250.0 else 0.0 // Simplified calculation
            ResponseEntity.ok(CheckAvailabilityResponse(
                available = available,
                roomId = request.roomId,
                startDate = request.startDate,
                endDate = request.endDate,
                totalPrice = totalPrice
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
    }

    @PostMapping
    fun createReservation(
        @RequestBody request: ReservationRequest,
        authentication: Authentication
    ): ResponseEntity<ReservationResponse> {
        return try {
            val userId = UserId(UUID.fromString(authentication.principal as String))
            val roomId = RoomId(request.roomId)
            val dateRange = DateRange(request.checkInDate, request.checkOutDate)

            val reservation = reservationService.createReservation(userId, roomId, dateRange)

            ResponseEntity.status(HttpStatus.CREATED).body(ReservationResponse(
                id = reservation.id.value,
                userId = reservation.userId.value,
                roomId = reservation.roomId.value,
                startDate = reservation.stay.startDate,
                endDate = reservation.stay.endDate,
                totalPrice = reservation.totalAmount.amount.toDouble(),
                status = reservation.status.toString(),
                createdAt = reservation.createdAt.toString()
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping("/{id}")
    fun getReservation(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ReservationResponse> {
        return try {
            val userId = UUID.fromString(authentication.principal as String)
            val reservation = reservationRepository.findById(ReservationId(id))
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)

            // Check authorization
            if (reservation.userId.value != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
            }

            ResponseEntity.ok(ReservationResponse(
                id = reservation.id.value,
                userId = reservation.userId.value,
                roomId = reservation.roomId.value,
                startDate = reservation.stay.startDate,
                endDate = reservation.stay.endDate,
                totalPrice = reservation.totalAmount.amount.toDouble(),
                status = reservation.status.toString(),
                createdAt = reservation.createdAt.toString()
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @GetMapping
    fun getUserReservations(authentication: Authentication): ResponseEntity<List<ReservationResponse>> {
        return try {
            val userId = UUID.fromString(authentication.principal as String)
            val reservations = reservationRepository.findByUserId(UserId(userId))
                .map { reservation ->
                    ReservationResponse(
                        id = reservation.id.value,
                        userId = reservation.userId.value,
                        roomId = reservation.roomId.value,
                        startDate = reservation.stay.startDate,
                        endDate = reservation.stay.endDate,
                        totalPrice = reservation.totalAmount.amount.toDouble(),
                        status = reservation.status.toString(),
                        createdAt = reservation.createdAt.toString()
                    )
                }

            ResponseEntity.ok(reservations)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emptyList())
        }
    }

    @DeleteMapping("/{id}")
    fun cancelReservation(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ReservationResponse> {
        return try {
            val userId = UUID.fromString(authentication.principal as String)
            val reservation = reservationRepository.findById(ReservationId(id))
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)

            // Check authorization
            if (reservation.userId.value != userId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
            }

            val cancelled = reservationService.cancelReservation(reservation.id)

            ResponseEntity.ok(ReservationResponse(
                id = cancelled.id.value,
                userId = cancelled.userId.value,
                roomId = cancelled.roomId.value,
                startDate = cancelled.stay.startDate,
                endDate = cancelled.stay.endDate,
                totalPrice = cancelled.totalAmount.amount.toDouble(),
                status = cancelled.status.toString(),
                createdAt = cancelled.createdAt.toString()
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }
}
