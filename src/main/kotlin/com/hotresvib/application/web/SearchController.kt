package com.hotresvib.application.web

import com.hotresvib.application.dto.*
import com.hotresvib.application.service.HotelSearchService
import com.hotresvib.application.service.PriceCalculationService
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.RoomId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/**
 * REST controller for search and discovery endpoints
 */
@RestController
@RequestMapping("/api/search")
class SearchController(
    private val hotelSearchService: HotelSearchService,
    private val priceCalculationService: PriceCalculationService
) {
    /**
     * Search hotels with various criteria
     * GET /api/search/hotels?city=Paris&checkIn=2026-03-01&checkOut=2026-03-05&page=0&size=20&sort=name,asc
     */
    @GetMapping("/hotels")
    fun searchHotels(
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) country: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkIn: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkOut: LocalDate?,
        @RequestParam(required = false) guests: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "name,asc") sort: String
    ): ResponseEntity<Page<HotelSearchResponse>> {
        // Validate pagination parameters
        require(page >= 0) { "Page index must not be negative" }
        require(size > 0 && size <= 100) { "Page size must be between 1 and 100" }
        
        // Parse sort parameter
        val sortParams = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortParams)
        
        // Create search criteria
        val criteria = HotelSearchCriteria(
            city = city,
            country = country,
            name = name,
            checkIn = checkIn,
            checkOut = checkOut,
            guests = guests
        )
        
        val result = hotelSearchService.searchHotels(criteria, pageable)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Search rooms with filtering
     * GET /api/search/rooms?type=DOUBLE&minPrice=50&maxPrice=200&available=true&checkIn=2026-03-01&checkOut=2026-03-05
     */
    @GetMapping("/rooms")
    fun searchRooms(
        @RequestParam(required = false) hotelId: String?,
        @RequestParam(required = false) type: RoomType?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkIn: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkOut: LocalDate?,
        @RequestParam(required = false) guests: Int?,
        @RequestParam(required = false) available: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "baseRate,asc") sort: String
    ): ResponseEntity<Page<RoomSearchResponse>> {
        // Validate pagination parameters
        require(page >= 0) { "Page index must not be negative" }
        require(size > 0 && size <= 100) { "Page size must be between 1 and 100" }
        
        // Parse sort parameter
        val sortParams = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortParams)
        
        // Create search criteria
        val criteria = RoomSearchCriteria(
            hotelId = hotelId?.let { HotelId(UUID.fromString(it)) },
            type = type,
            minPrice = minPrice,
            maxPrice = maxPrice,
            checkIn = checkIn,
            checkOut = checkOut,
            guests = guests,
            available = available
        )
        
        val result = hotelSearchService.searchRooms(criteria, pageable)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Search available rooms with date range (returns rooms with calculated total price)
     * GET /api/search/available-rooms?checkIn=2026-03-01&checkOut=2026-03-05&city=Paris
     */
    @GetMapping("/available-rooms")
    fun searchAvailableRooms(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkIn: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkOut: LocalDate,
        @RequestParam(required = false) city: String?,
        @RequestParam(required = false) country: String?,
        @RequestParam(required = false) type: RoomType?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) guests: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "totalPrice,asc") sort: String
    ): ResponseEntity<Page<RoomAvailabilityResponse>> {
        // Validate pagination parameters
        require(page >= 0) { "Page index must not be negative" }
        require(size > 0 && size <= 100) { "Page size must be between 1 and 100" }
        
        // Parse sort parameter
        val sortParams = parseSort(sort)
        val pageable = PageRequest.of(page, size, sortParams)
        
        // Create search criteria (will validate dates)
        val criteria = AvailabilitySearchCriteria(
            checkIn = checkIn,
            checkOut = checkOut,
            city = city,
            country = country,
            type = type,
            minPrice = minPrice,
            maxPrice = maxPrice,
            guests = guests
        )
        
        val result = hotelSearchService.searchAvailableRooms(criteria, pageable)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Get featured hotels
     * GET /api/hotels/featured?page=0&size=10
     */
    @GetMapping("/featured")
    fun getFeaturedHotels(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Page<HotelSearchResponse>> {
        require(page >= 0) { "Page index must not be negative" }
        require(size > 0 && size <= 100) { "Page size must be between 1 and 100" }
        
        val pageable = PageRequest.of(page, size)
        val result = hotelSearchService.getFeaturedHotels(pageable)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Autocomplete for hotel names and cities
     * GET /api/search/autocomplete?query=par
     */
    @GetMapping("/autocomplete")
    fun autocomplete(
        @RequestParam query: String
    ): ResponseEntity<List<AutocompleteSuggestion>> {
        val result = hotelSearchService.autocomplete(query)
        return ResponseEntity.ok(result)
    }
    
    /**
     * Calculate price for a specific room and date range
     * GET /api/search/price?roomId=...&checkIn=2026-03-01&checkOut=2026-03-05
     */
    @GetMapping("/price")
    fun calculatePrice(
        @RequestParam roomId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkIn: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) checkOut: LocalDate
    ): ResponseEntity<PriceBreakdown> {
        val roomIdValue = RoomId(UUID.fromString(roomId))
        val breakdown = priceCalculationService.calculatePriceBreakdown(roomIdValue, checkIn, checkOut)
        
        val response = PriceBreakdown(
            basePrice = breakdown.basePrice.amount,
            currency = breakdown.basePrice.currency,
            nights = breakdown.nights,
            subtotal = breakdown.subtotal.amount,
            pricingRulesApplied = breakdown.pricingRulesApplied,
            total = breakdown.total.amount
        )
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * Parse sort parameter (e.g., "name,asc" or "price,desc")
     */
    private fun parseSort(sort: String): Sort {
        val parts = sort.split(",")
        require(parts.size == 2) { "Sort parameter must be in format: field,direction" }
        
        val field = parts[0]
        val direction = when (parts[1].lowercase()) {
            "asc" -> Sort.Direction.ASC
            "desc" -> Sort.Direction.DESC
            else -> throw IllegalArgumentException("Sort direction must be 'asc' or 'desc'")
        }
        
        return Sort.by(direction, field)
    }
}
