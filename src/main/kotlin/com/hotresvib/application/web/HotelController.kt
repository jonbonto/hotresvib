package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.port.*
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.Money
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import java.math.BigDecimal

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = ["*"])
class HotelController(
    private val hotelRepository: HotelRepository,
    private val roomRepository: RoomRepository,
    private val reviewRepository: ReviewRepository
) {

    @GetMapping
    fun listHotels(): ResponseEntity<List<HotelResponse>> {
        return try {
            val hotels = hotelRepository.findAll().map { hotel ->
                HotelResponse(
                    id = hotel.id.value,
                    name = hotel.name.value,
                    city = hotel.city,
                    country = hotel.country
                )
            }
            ResponseEntity.ok(hotels)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list hotels")
        }
    }

    @GetMapping("/{id}")
    fun getHotel(@PathVariable id: UUID): ResponseEntity<HotelDetailResponse> {
        return try {
            val hotel = hotelRepository.findAll().find { it.id.value == id }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found")

            val roomCount = roomRepository.findAll().count { it.hotelId == hotel.id }
            val avgRating = reviewRepository.averageRatingByHotelId(hotel.id) ?: 0.0
            val reviewCount = reviewRepository.countByHotelId(hotel.id)

            ResponseEntity.ok(HotelDetailResponse(
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
            ))
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve hotel")
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createHotel(@RequestBody request: HotelRequest): ResponseEntity<HotelDetailResponse> {
        return try {
            val hotel = Hotel(
                id = HotelId.generate(),
                name = HotelName(request.name),
                city = request.city,
                country = request.country,
                description = request.description,
                address = request.address,
                phone = request.phone,
                email = request.email,
                starRating = request.starRating ?: 0,
                isFeatured = request.isFeatured ?: false,
                imageUrl = request.imageUrl
            )
            hotelRepository.save(hotel)

            ResponseEntity.status(HttpStatus.CREATED).body(HotelDetailResponse(
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
                imageUrl = hotel.imageUrl
            ))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid hotel data")
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create hotel")
        }
    }

    @GetMapping("/{hotelId}/rooms")
    fun listHotelRooms(@PathVariable hotelId: UUID): ResponseEntity<List<RoomResponse>> {
        return try {
            val rooms = roomRepository.findAll()
                .filter { it.hotelId.value == hotelId }
                .map { room ->
                    RoomResponse(
                        id = room.id.value,
                        hotelId = room.hotelId.value,
                        number = room.number.value,
                        type = room.type.toString(),
                        basePrice = room.baseRate.amount.toDouble(),
                        currency = room.baseRate.currency,
                        description = room.description,
                        capacity = room.capacity,
                        amenities = room.amenities,
                        imageUrl = room.imageUrl
                    )
                }
            ResponseEntity.ok(rooms)
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to list hotel rooms")
        }
    }

    @PostMapping("/{hotelId}/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    fun createRoom(
        @PathVariable hotelId: UUID,
        @RequestBody request: RoomRequest
    ): ResponseEntity<RoomResponse> {
        return try {
            hotelRepository.findById(HotelId(hotelId))
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found")

            val room = Room(
                id = RoomId.generate(),
                hotelId = HotelId(hotelId),
                number = RoomNumber(request.number),
                type = RoomType.valueOf(request.type),
                baseRate = Money(request.basePrice.toBigDecimal(), request.currency),
                description = request.description,
                capacity = request.capacity,
                amenities = request.amenities,
                imageUrl = request.imageUrl
            )
            roomRepository.save(room)

            ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse(
                id = room.id.value,
                hotelId = room.hotelId.value,
                number = room.number.value,
                type = room.type.toString(),
                basePrice = room.baseRate.amount.toDouble(),
                currency = room.baseRate.currency,
                description = room.description,
                capacity = room.capacity,
                amenities = room.amenities,
                imageUrl = room.imageUrl
            ))
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message ?: "Invalid room data")
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create room")
        }
    }
}
