package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.service.ReservationApplicationService
import com.hotresvib.application.service.AvailabilityApplicationService
import com.hotresvib.application.port.*
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.shared.DateRange
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = ["*"])
class ReservationController(
    private val reservationService: ReservationApplicationService,
    private val availabilityService: AvailabilityApplicationService,
    private val priceCalculationService: com.hotresvib.application.service.PriceCalculationService,
    private val reservationRepository: ReservationRepository,
    private val roomRepository: RoomRepository
) {

    @PostMapping("/check-availability")
    fun checkAvailability(@RequestBody request: CheckAvailabilityRequest): ResponseEntity<CheckAvailabilityResponse> {
        return try {
            // validate that the room actually exists before checking availability
            val roomExists = roomRepository.findById(RoomId(request.roomId)) != null
            if (!roomExists) {
                // treat non‑existent room as unavailable (could also return 400/404)
                return ResponseEntity.ok(
                    CheckAvailabilityResponse(
                        available = false,
                        roomId = request.roomId,
                        startDate = request.startDate,
                        endDate = request.endDate,
                        totalPrice = 0.0
                    )
                )
            }

            val available = availabilityService.checkAvailability(
                RoomId(request.roomId),
                request.startDate,
                request.endDate
            )
            val totalPrice = if (available) {
                // calculate using priceCalc service
                val price = priceCalculationService.calculateTotalPrice(
                    RoomId(request.roomId), request.startDate, request.endDate
                )
                price.amount.toDouble()
            } else 0.0
            ResponseEntity.ok(CheckAvailabilityResponse(
                available = available,
                roomId = request.roomId,
                startDate = request.startDate,
                endDate = request.endDate,
                totalPrice = totalPrice
            ))
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to check availability")
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    fun createReservation(
        @RequestBody request: ReservationRequest,
        authentication: Authentication
    ): ResponseEntity<ReservationResponse> {
        return try {
            val userId = try {
                UserId(UUID.fromString(authentication.principal as String))
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
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
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid request")
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create reservation")
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    fun getReservation(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ReservationResponse> {
        return try {
            val userId = try {
                UUID.fromString(authentication.principal as String)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
            val reservation = reservationRepository.findById(ReservationId(id))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")

            // Check authorization
            if (reservation.userId.value != userId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to access this reservation")
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
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve reservation")
        }
    }

    @GetMapping
    fun getUserReservations(authentication: Authentication): ResponseEntity<List<ReservationResponse>> {
        return try {
            val userId = try {
                UUID.fromString(authentication.principal as String)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
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
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve reservations")
        }
    }

    @DeleteMapping("/{id}")
    fun cancelReservation(
        @PathVariable id: UUID,
        authentication: Authentication
    ): ResponseEntity<ReservationResponse> {
        return try {
            val userId = try {
                UUID.fromString(authentication.principal as String)
            } catch (e: IllegalArgumentException) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user ID")
            }
            val reservation = reservationRepository.findById(ReservationId(id))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")

            // Check authorization
            if (reservation.userId.value != userId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "No permission to cancel this reservation")
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
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel reservation")
        }
    }
}
