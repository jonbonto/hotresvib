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
    private val roomRepository: RoomRepository
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
    fun getHotel(@PathVariable id: UUID): ResponseEntity<HotelResponse> {
        return try {
            // Use a safe lookup by comparing raw UUID to avoid potential repository ID conversion issues
            val hotel = hotelRepository.findAll().find { it.id.value == id }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found")

            ResponseEntity.ok(HotelResponse(
                id = hotel.id.value,
                name = hotel.name.value,
                city = hotel.city,
                country = hotel.country
            ))
        } catch (e: ResponseStatusException) {
            throw e
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve hotel")
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createHotel(@RequestBody request: HotelRequest): ResponseEntity<HotelResponse> {
        return try {
            val hotel = Hotel(
                id = HotelId.generate(),
                name = HotelName(request.name),
                city = request.city,
                country = request.country
            )
            hotelRepository.save(hotel)

            ResponseEntity.status(HttpStatus.CREATED).body(HotelResponse(
                id = hotel.id.value,
                name = hotel.name.value,
                city = hotel.city,
                country = hotel.country
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
                        currency = room.baseRate.currency
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
                baseRate = Money(request.basePrice.toBigDecimal(), request.currency)
            )
            roomRepository.save(room)

            ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse(
                id = room.id.value,
                hotelId = room.hotelId.value,
                number = room.number.value,
                type = room.type.toString(),
                basePrice = room.baseRate.amount.toDouble(),
                currency = room.baseRate.currency
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
