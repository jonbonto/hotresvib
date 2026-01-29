package com.hotresvib.application.service

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Service for calculating room prices with pricing rules applied
 */
@Service
class PriceCalculationService(
    private val roomRepository: RoomRepository,
    private val pricingRuleRepository: PricingRuleRepository,
    private val availabilityRepository: AvailabilityRepository
) {
    /**
     * Calculate total price for a room stay
     */
    fun calculateTotalPrice(roomId: RoomId, checkIn: LocalDate, checkOut: LocalDate): Money {
        val room = roomRepository.findById(roomId)
            ?: throw IllegalArgumentException("Room not found: $roomId")
        
        val nights = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()
        require(nights > 0) { "Check-out must be after check-in" }
        
        // Start with base price for all nights
        var totalAmount = room.baseRate.amount.multiply(BigDecimal.valueOf(nights.toLong()))
        val currency = room.baseRate.currency
        
        val dateRange = DateRange(checkIn, checkOut)
        
        // Get all pricing rules for the room
        val pricingRules = pricingRuleRepository.findByRoomId(roomId)
        
        // Apply applicable pricing rules
        for (rule in pricingRules) {
            if (dateRange.overlaps(rule.range)) {
                // Calculate overlap days
                val overlapStart = maxOf(dateRange.startDate, rule.range.startDate)
                val overlapEnd = minOf(dateRange.endDate, rule.range.endDate)
                val overlapNights = ChronoUnit.DAYS.between(overlapStart, overlapEnd).toInt()
                
                if (overlapNights > 0) {
                    // Apply the pricing rule's price for the overlapping days
                    val rulePrice = rule.price.amount.multiply(BigDecimal.valueOf(overlapNights.toLong()))
                    val basePrice = room.baseRate.amount.multiply(BigDecimal.valueOf(overlapNights.toLong()))
                    val difference = rulePrice.subtract(basePrice)
                    totalAmount = totalAmount.add(difference)
                }
            }
        }
        
        return Money(totalAmount, currency)
    }
    
    /**
     * Calculate price breakdown with details
     */
    fun calculatePriceBreakdown(roomId: RoomId, checkIn: LocalDate, checkOut: LocalDate): PriceBreakdownDetails {
        val room = roomRepository.findById(roomId)
            ?: throw IllegalArgumentException("Room not found: $roomId")
        
        val nights = ChronoUnit.DAYS.between(checkIn, checkOut).toInt()
        val dateRange = DateRange(checkIn, checkOut)
        val pricingRules = pricingRuleRepository.findByRoomId(roomId)
        
        val appliedRules = mutableListOf<String>()
        var totalAmount = room.baseRate.amount.multiply(BigDecimal.valueOf(nights.toLong()))
        val subtotalAmount = totalAmount
        val currency = room.baseRate.currency
        
        for (rule in pricingRules) {
            if (dateRange.overlaps(rule.range)) {
                val overlapStart = maxOf(dateRange.startDate, rule.range.startDate)
                val overlapEnd = minOf(dateRange.endDate, rule.range.endDate)
                val overlapNights = ChronoUnit.DAYS.between(overlapStart, overlapEnd).toInt()
                
                if (overlapNights > 0) {
                    val rulePrice = rule.price.amount.multiply(BigDecimal.valueOf(overlapNights.toLong()))
                    val basePrice = room.baseRate.amount.multiply(BigDecimal.valueOf(overlapNights.toLong()))
                    val difference = rulePrice.subtract(basePrice)
                    totalAmount = totalAmount.add(difference)
                    
                    appliedRules.add("${rule.range.startDate} to ${rule.range.endDate}: ${rule.price.amount} ${rule.price.currency}")
                }
            }
        }
        
        return PriceBreakdownDetails(
            basePrice = room.baseRate,
            nights = nights,
            subtotal = Money(subtotalAmount, currency),
            pricingRulesApplied = appliedRules,
            total = Money(totalAmount, currency)
        )
    }
}

/**
 * Price breakdown details
 */
data class PriceBreakdownDetails(
    val basePrice: Money,
    val nights: Int,
    val subtotal: Money,
    val pricingRulesApplied: List<String>,
    val total: Money
)
