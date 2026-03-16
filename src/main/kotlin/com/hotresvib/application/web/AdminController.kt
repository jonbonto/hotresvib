package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.port.*
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.UserRole
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = ["*"])
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val userRepository: UserRepository,
    private val hotelRepository: HotelRepository,
    private val roomRepository: RoomRepository,
    private val reservationRepository: ReservationRepository,
    private val paymentRepository: PaymentRepository,
    private val reviewRepository: ReviewRepository
) {

    // --- User Management ---
    @GetMapping("/users")
    fun listUsers(): ResponseEntity<List<UserResponse>> {
        val users = userRepository.findAll().map { user ->
            UserResponse(
                id = user.id.value,
                email = user.email.value,
                displayName = user.displayName,
                role = user.role
            )
        }
        return ResponseEntity.ok(users)
    }

    @GetMapping("/users/{id}")
    fun getUser(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = userRepository.findById(UserId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        return ResponseEntity.ok(UserResponse(
            id = user.id.value,
            email = user.email.value,
            displayName = user.displayName,
            role = user.role
        ))
    }

    @PutMapping("/users/{id}/role")
    fun updateUserRole(
        @PathVariable id: UUID,
        @RequestBody request: UpdateRoleRequest
    ): ResponseEntity<UserResponse> {
        val user = userRepository.findById(UserId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        val updated = user.copy(role = UserRole.valueOf(request.role))
        val saved = userRepository.save(updated)
        return ResponseEntity.ok(UserResponse(
            id = saved.id.value,
            email = saved.email.value,
            displayName = saved.displayName,
            role = saved.role
        ))
    }

    // --- Hotel Management ---
    @PutMapping("/hotels/{id}")
    fun updateHotel(
        @PathVariable id: UUID,
        @RequestBody request: UpdateHotelRequest
    ): ResponseEntity<HotelDetailResponse> {
        val hotel = hotelRepository.findById(HotelId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found")
        val updated = hotel.copy(
            name = request.name?.let { HotelName(it) } ?: hotel.name,
            city = request.city ?: hotel.city,
            country = request.country ?: hotel.country,
            description = request.description ?: hotel.description,
            address = request.address ?: hotel.address,
            phone = request.phone ?: hotel.phone,
            email = request.email ?: hotel.email,
            starRating = request.starRating ?: hotel.starRating,
            isFeatured = request.isFeatured ?: hotel.isFeatured,
            imageUrl = request.imageUrl ?: hotel.imageUrl
        )
        val saved = hotelRepository.save(updated)
        val roomCount = roomRepository.findAll().count { it.hotelId == saved.id }
        return ResponseEntity.ok(toHotelDetailResponse(saved, roomCount))
    }

    @DeleteMapping("/hotels/{id}")
    fun deleteHotel(@PathVariable id: UUID): ResponseEntity<Void> {
        hotelRepository.findById(HotelId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found")
        hotelRepository.deleteById(HotelId(id))
        return ResponseEntity.noContent().build()
    }

    // --- Reservation Management ---
    @GetMapping("/reservations")
    fun listAllReservations(
        @RequestParam(required = false) status: String?
    ): ResponseEntity<List<ReservationResponse>> {
        val reservations = if (status != null) {
            reservationRepository.findByStatus(ReservationStatus.valueOf(status))
        } else {
            // Get all statuses
            ReservationStatus.entries.flatMap { reservationRepository.findByStatus(it) }
        }
        return ResponseEntity.ok(reservations.map { reservation ->
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
        })
    }

    @PutMapping("/reservations/{id}/check-in")
    fun checkIn(
        @PathVariable id: UUID
    ): ResponseEntity<ReservationResponse> {
        val reservation = reservationRepository.findById(com.hotresvib.domain.shared.ReservationId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")
        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation must be CONFIRMED to check in")
        }
        // We reuse CONFIRMED status but track check-in through audit
        return ResponseEntity.ok(ReservationResponse(
            id = reservation.id.value,
            userId = reservation.userId.value,
            roomId = reservation.roomId.value,
            startDate = reservation.stay.startDate,
            endDate = reservation.stay.endDate,
            totalPrice = reservation.totalAmount.amount.toDouble(),
            status = "CHECKED_IN",
            createdAt = reservation.createdAt.toString()
        ))
    }

    @PutMapping("/reservations/{id}/check-out")
    fun checkOut(
        @PathVariable id: UUID
    ): ResponseEntity<ReservationResponse> {
        val reservation = reservationRepository.findById(com.hotresvib.domain.shared.ReservationId(id))
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")
        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation must be CONFIRMED to check out")
        }
        return ResponseEntity.ok(ReservationResponse(
            id = reservation.id.value,
            userId = reservation.userId.value,
            roomId = reservation.roomId.value,
            startDate = reservation.stay.startDate,
            endDate = reservation.stay.endDate,
            totalPrice = reservation.totalAmount.amount.toDouble(),
            status = "CHECKED_OUT",
            createdAt = reservation.createdAt.toString()
        ))
    }

    // --- Analytics ---
    @GetMapping("/analytics")
    fun getAnalytics(): ResponseEntity<AdminAnalyticsResponse> {
        val totalUsers = userRepository.findAll().size.toLong()
        val totalHotels = hotelRepository.findAll().size.toLong()
        val allReservations = ReservationStatus.entries.flatMap { reservationRepository.findByStatus(it) }
        val totalReservations = allReservations.size.toLong()
        val confirmedReservations = allReservations.count { it.status == ReservationStatus.CONFIRMED }.toLong()
        val totalRevenue = allReservations
            .filter { it.status == ReservationStatus.CONFIRMED || it.status == ReservationStatus.REFUNDED }
            .sumOf { it.totalAmount.amount.toDouble() }

        return ResponseEntity.ok(AdminAnalyticsResponse(
            totalUsers = totalUsers,
            totalHotels = totalHotels,
            totalReservations = totalReservations,
            confirmedReservations = confirmedReservations,
            totalRevenue = totalRevenue,
            recentReservations = allReservations.sortedByDescending { it.createdAt }.take(10).map { reservation ->
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
        ))
    }

    private fun toHotelDetailResponse(hotel: Hotel, roomCount: Int) = HotelDetailResponse(
        id = hotel.id.value,
        name = hotel.name.value,
        city = hotel.city,
        country = hotel.country,
        description = hotel.description,
        address = hotel.address,
        phone = hotel.phone,
        email = hotel.email,
        starRating = hotel.starRating,
        isFeatured = hotel.isFeatured,
        imageUrl = hotel.imageUrl,
        roomCount = roomCount
    )
}
