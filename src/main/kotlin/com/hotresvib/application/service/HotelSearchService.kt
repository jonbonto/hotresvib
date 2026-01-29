package com.hotresvib.application.service

import com.hotresvib.application.dto.*
import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.HotelRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

/**
 * Service for searching hotels and rooms
 */
@Service
class HotelSearchService(
    private val hotelRepository: HotelRepository,
    private val roomRepository: RoomRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val priceCalculationService: PriceCalculationService
) {
    /**
     * Search hotels based on criteria
     */
    fun searchHotels(criteria: HotelSearchCriteria, pageable: Pageable): Page<HotelSearchResponse> {
        val hotels = if (criteria.city != null || criteria.country != null || criteria.name != null) {
            hotelRepository.searchByCriteria(
                city = criteria.city,
                country = criteria.country,
                name = criteria.name,
                pageable = pageable
            )
        } else {
            hotelRepository.findAllPaged(pageable)
        }
        
        return hotels.map { hotel ->
            val rooms = roomRepository.findByHotelId(hotel.id)
            val roomCount = rooms.size
            val minPrice = rooms.minOfOrNull { it.baseRate.amount }
            
            HotelSearchResponse.from(hotel, roomCount, minPrice, hotel.isFeatured)
        }
    }
    
    /**
     * Search rooms based on criteria
     */
    fun searchRooms(criteria: RoomSearchCriteria, pageable: Pageable): Page<RoomSearchResponse> {
        val rooms = roomRepository.searchByCriteria(
            hotelId = criteria.hotelId,
            type = criteria.type,
            minPrice = criteria.minPrice,
            maxPrice = criteria.maxPrice,
            pageable = pageable
        )
        
        return rooms.map { room ->
            val hotel = hotelRepository.findById(room.hotelId)
                ?: throw IllegalStateException("Hotel not found for room: ${room.id}")
            
            val isAvailable = if (criteria.available == true && criteria.checkIn != null && criteria.checkOut != null) {
                checkRoomAvailability(room.id, criteria.checkIn!!, criteria.checkOut!!)
            } else {
                null
            }
            
            RoomSearchResponse.from(room, hotel, isAvailable)
        }
    }
    
    /**
     * Search for available rooms with calculated pricing
     */
    fun searchAvailableRooms(criteria: AvailabilitySearchCriteria, pageable: Pageable): Page<RoomAvailabilityResponse> {
        // Get all rooms matching the criteria (without pagination first)
        val allRooms = if (criteria.city != null || criteria.country != null) {
            // Filter hotels by location first
            val hotels = hotelRepository.searchByCriteria(
                city = criteria.city,
                country = criteria.country,
                name = null,
                pageable = Pageable.unpaged()
            ).content
            
            val hotelIds = hotels.map { it.id }
            roomRepository.findAll().filter { it.hotelId in hotelIds }
        } else {
            roomRepository.findAll()
        }
        
        // Filter by room criteria
        val filteredRooms = allRooms.filter { room ->
            var matches = true
            
            if (criteria.type != null) {
                matches = matches && room.type == criteria.type
            }
            
            if (criteria.minPrice != null) {
                matches = matches && room.baseRate.amount >= criteria.minPrice
            }
            
            if (criteria.maxPrice != null) {
                matches = matches && room.baseRate.amount <= criteria.maxPrice
            }
            
            matches
        }
        
        // Check availability and calculate prices
        val availableRooms = filteredRooms.mapNotNull { room ->
            if (checkRoomAvailability(room.id, criteria.checkIn, criteria.checkOut)) {
                val hotel = hotelRepository.findById(room.hotelId)
                    ?: return@mapNotNull null
                
                val totalPrice = priceCalculationService.calculateTotalPrice(
                    room.id,
                    criteria.checkIn,
                    criteria.checkOut
                )
                
                val nights = ChronoUnit.DAYS.between(criteria.checkIn, criteria.checkOut).toInt()
                
                RoomAvailabilityResponse.from(room, hotel, totalPrice, nights)
            } else {
                null
            }
        }
        
        // Apply pagination manually
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, availableRooms.size)
        val pageContent = if (start < availableRooms.size) {
            availableRooms.subList(start, end)
        } else {
            emptyList()
        }
        
        return PageImpl(pageContent, pageable, availableRooms.size.toLong())
    }
    
    /**
     * Get featured hotels
     */
    fun getFeaturedHotels(pageable: Pageable): Page<HotelSearchResponse> {
        val hotels = hotelRepository.findByIsFeatured(true, pageable)
        
        return hotels.map { hotel ->
            val rooms = roomRepository.findByHotelId(hotel.id)
            val roomCount = rooms.size
            val minPrice = rooms.minOfOrNull { it.baseRate.amount }
            
            HotelSearchResponse.from(hotel, roomCount, minPrice, true)
        }
    }
    
    /**
     * Autocomplete search for hotels and cities
     */
    fun autocomplete(query: String): List<AutocompleteSuggestion> {
        require(query.length >= 2) { "Query must be at least 2 characters" }
        
        val allHotels = hotelRepository.findAll()
        val suggestions = mutableListOf<AutocompleteSuggestion>()
        
        // Search hotel names
        allHotels.filter { it.name.value.contains(query, ignoreCase = true) }
            .take(5)
            .forEach { hotel ->
                suggestions.add(
                    AutocompleteSuggestion(
                        type = "hotel",
                        value = hotel.id.value.toString(),
                        displayText = "${hotel.name.value} - ${hotel.city}, ${hotel.country}"
                    )
                )
            }
        
        // Search cities
        allHotels.map { it.city }
            .distinct()
            .filter { it.contains(query, ignoreCase = true) }
            .take(5)
            .forEach { city ->
                suggestions.add(
                    AutocompleteSuggestion(
                        type = "city",
                        value = city,
                        displayText = city
                    )
                )
            }
        
        return suggestions.take(10)
    }
    
    /**
     * Check if a room is available for the given date range
     */
    private fun checkRoomAvailability(roomId: com.hotresvib.domain.shared.RoomId, checkIn: java.time.LocalDate, checkOut: java.time.LocalDate): Boolean {
        val availabilities = availabilityRepository.findByRoomIdAndDateRange(roomId, checkIn, checkOut)
        
        // Room is available if we have an availability record with available.value > 0
        return availabilities.any { it.available.value > 0 }
    }
}
