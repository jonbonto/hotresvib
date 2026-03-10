package com.hotresvib.application.service

import com.hotresvib.application.port.PricingRuleRepository
import com.hotresvib.application.port.RoomRepository
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.pricing.PricingRule
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Service for calculating room prices with pricing rules applied
 */
@Service
class PriceCalculationService(
    private val roomRepository: RoomRepository,
    private val pricingRuleRepository: PricingRuleRepository
) {
    /**
     * Calculate total price for a room stay
     */
    fun calculateTotalPrice(roomId: RoomId, checkIn: LocalDate, checkOut: LocalDate): Money {
        val room = roomRepository.findById(roomId)
            ?: throw IllegalArgumentException("Room not found: $roomId")
        val pricingRules = pricingRuleRepository.findByRoomId(roomId)
        return calculateTotalAmount(room, DateRange(checkIn, checkOut), pricingRules)
    }

    fun calculateTotalAmount(room: Room, stay: DateRange, pricingRules: List<PricingRule>): Money {
        val nightlyRates = resolveNightlyRates(room, stay, pricingRules)
        val totalAmount = nightlyRates.fold(BigDecimal.ZERO) { total, nightlyRate ->
            total.add(nightlyRate.price.amount)
        }

        return Money(totalAmount, room.baseRate.currency)
    }
    
    /**
     * Calculate price breakdown with details
     */
    fun calculatePriceBreakdown(roomId: RoomId, checkIn: LocalDate, checkOut: LocalDate): PriceBreakdownDetails {
        val room = roomRepository.findById(roomId)
            ?: throw IllegalArgumentException("Room not found: $roomId")
        val dateRange = DateRange(checkIn, checkOut)
        val pricingRules = pricingRuleRepository.findByRoomId(roomId)
        val nightlyRates = resolveNightlyRates(room, dateRange, pricingRules)
        val subtotalAmount = room.baseRate.amount.multiply(BigDecimal.valueOf(dateRange.nights.toLong()))
        val totalAmount = nightlyRates.fold(BigDecimal.ZERO) { total, nightlyRate ->
            total.add(nightlyRate.price.amount)
        }
        val appliedRules = nightlyRates
            .groupBy { it.source?.id }
            .values
            .mapNotNull { ratesForRule ->
                val sourceRule = ratesForRule.first().source ?: return@mapNotNull null
                val appliedStart = ratesForRule.first().date
                val appliedEnd = ratesForRule.last().date.plusDays(1)
                val descriptionSuffix = sourceRule.description?.let { " ($it)" } ?: ""
                "$appliedStart to $appliedEnd: ${sourceRule.price.amount} ${sourceRule.price.currency}$descriptionSuffix"
            }
        
        return PriceBreakdownDetails(
            basePrice = room.baseRate,
            nights = dateRange.nights,
            subtotal = Money(subtotalAmount, room.baseRate.currency),
            pricingRulesApplied = appliedRules,
            total = Money(totalAmount, room.baseRate.currency)
        )
    }

    private fun resolveNightlyRates(room: Room, stay: DateRange, pricingRules: List<PricingRule>): List<NightlyRate> {
        require(stay.startDate.isBefore(stay.endDate)) { "Check-out must be after check-in" }

        val nightlyRates = mutableListOf<NightlyRate>()
        var cursor = stay.startDate

        while (cursor.isBefore(stay.endDate)) {
            val nightlyStay = DateRange(cursor, cursor.plusDays(1))
            val applicableRule = pricingRules
                .filter { it.range.overlaps(nightlyStay) }
                .maxWithOrNull(
                    compareBy<PricingRule> { it.range.startDate }
                        .thenBy { it.range.endDate }
                )

            applicableRule?.let {
                require(it.price.currency == room.baseRate.currency) {
                    "Pricing rule currency must match room base rate currency"
                }
            }

            nightlyRates += NightlyRate(
                date = cursor,
                price = applicableRule?.price ?: room.baseRate,
                source = applicableRule
            )
            cursor = cursor.plusDays(1)
        }

        return nightlyRates
    }
}

private data class NightlyRate(
    val date: LocalDate,
    val price: Money,
    val source: PricingRule?
)

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
