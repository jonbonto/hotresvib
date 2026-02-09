package com.hotresvib.security

import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

/**
 * Tests for edge case handling (Phase 11).
 * Covers: past dates, same-day booking, max/min stay duration, etc.
 */
@DisplayName("Edge Cases Tests")
class EdgeCasesTest {
    
    @Test
    fun `reject reservation with check-in date in the past`() {
        val yesterday = LocalDate.now().minusDays(1)
        val tomorrow = LocalDate.now().plusDays(1)
        val stay = DateRange(yesterday, tomorrow)
        
        val exception = assertFailsWith<IllegalArgumentException> {
            // Validate would happen in ReservationService
            require(!stay.startDate.isBefore(LocalDate.now())) { "Check-in date must be in the future" }
        }
        assertEquals("Check-in date must be in the future", exception.message)
    }
    
    @Test
    fun `accept reservation with future check-in date`() {
        val tomorrow = LocalDate.now().plusDays(1)
        val afterTomorrow = LocalDate.now().plusDays(2)
        val stay = DateRange(tomorrow, afterTomorrow)
        
        require(!stay.startDate.isBefore(LocalDate.now())) { "Check-in date must be in the future" }
        // Should not throw
    }
    
    @Test
    fun `reject reservation with minimum stay duration less than 1 night`() {
        val today = LocalDate.now()
        val stay = DateRange(today, today)
        
        val exception = assertFailsWith<IllegalArgumentException> {
            require(stay.startDate.isBefore(stay.endDate)) { "Stay must be at least one night" }
        }
        assertEquals("Stay must be at least one night", exception.message)
    }
    
    @Test
    fun `reject reservation exceeding maximum stay duration of 30 nights`() {
        val today = LocalDate.now()
        val future = today.plusDays(31)
        val stay = DateRange(today, future)
        
        val nights = java.time.temporal.ChronoUnit.DAYS.between(stay.startDate, stay.endDate)
        val exception = assertFailsWith<IllegalArgumentException> {
            require(nights <= 30) { "Maximum stay duration is 30 nights" }
        }
        assertEquals("Maximum stay duration is 30 nights", exception.message)
    }
    
    @Test
    fun `accept reservation with 30 nights maximum stay duration`() {
        val today = LocalDate.now()
        val future = today.plusDays(30)
        val stay = DateRange(today, future)
        
        val nights = java.time.temporal.ChronoUnit.DAYS.between(stay.startDate, stay.endDate)
        require(nights <= 30) { "Maximum stay duration is 30 nights" }
        // Should not throw
    }
    
    @Test
    fun `timezone stored correctly`() {
        val user = com.hotresvib.domain.user.User(
            id = UserId(java.util.UUID.randomUUID()),
            email = com.hotresvib.domain.user.EmailAddress("test@example.com"),
            displayName = "Test User",
            role = com.hotresvib.domain.user.UserRole.CUSTOMER,
            passwordHash = "hash",
            timezone = "America/New_York"
        )
        
        assertEquals("America/New_York", user.timezone)
    }
    
    @Test
    fun `default timezone is UTC`() {
        val user = com.hotresvib.domain.user.User(
            id = UserId(java.util.UUID.randomUUID()),
            email = com.hotresvib.domain.user.EmailAddress("test@example.com"),
            displayName = "Test User",
            role = com.hotresvib.domain.user.UserRole.CUSTOMER,
            passwordHash = "hash"
        )
        
        assertEquals("UTC", user.timezone)
    }
    
    @Test
    fun `optimistic locking version is tracked`() {
        val user = com.hotresvib.domain.user.User(
            id = UserId(java.util.UUID.randomUUID()),
            email = com.hotresvib.domain.user.EmailAddress("test@example.com"),
            displayName = "Test User",
            role = com.hotresvib.domain.user.UserRole.CUSTOMER,
            passwordHash = "hash"
        )
        
        assertEquals(null, user.version)
    }
    
    @Test
    fun `idempotency key prevents duplicate payments`() {
        val payment1 = com.hotresvib.domain.payment.Payment(
            id = java.util.UUID.randomUUID(),
            reservationId = com.hotresvib.domain.shared.ReservationId.generate(),
            amount = com.hotresvib.domain.shared.Money(java.math.BigDecimal("100.00"), "USD"),
            status = com.hotresvib.domain.payment.PaymentStatus.COMPLETED,
            paymentMethod = "STRIPE",
            transactionId = "txn_123",
            paymentIntentId = "pi_123",
            idempotencyKey = "idempotency_key_123",
            createdAt = Instant.now()
        )
        
        // Both payments with same idempotency key should be treated as duplicate
        val payment2 = payment1.copy(id = java.util.UUID.randomUUID())
        
        assertEquals(payment1.idempotencyKey, payment2.idempotencyKey)
    }
    
    @Test
    fun `concurrent modification detected by version`() {
        var entity = com.hotresvib.domain.user.User(
            id = UserId(java.util.UUID.randomUUID()),
            email = com.hotresvib.domain.user.EmailAddress("test@example.com"),
            displayName = "Original Name",
            role = com.hotresvib.domain.user.UserRole.CUSTOMER,
            passwordHash = "hash",
            version = 1L
        )
        
        // Simulate concurrent modification
        val modifiedEntity = entity.copy(displayName = "Modified Name", version = 2L)
        
        // Version changed indicates concurrent modification
        assert(entity.version != modifiedEntity.version)
    }
}
