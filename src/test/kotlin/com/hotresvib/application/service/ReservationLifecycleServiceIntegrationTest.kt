package com.hotresvib.application.service

import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.availability.AvailableQuantity
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.*
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.integration.DatabaseIntegrationTestBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Integration tests for the reservation lifecycle service to reproduce
 * the bug where JPA would complain about ID types when expiring a
 * reservation read from the database.  This guards against regressions.
 */
@Transactional
class ReservationLifecycleServiceIntegrationTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var reservationService: ReservationApplicationService

    @Autowired
    private lateinit var reservationRepository: com.hotresvib.application.port.ReservationRepository

    @Autowired
    private lateinit var userRepository: com.hotresvib.infrastructure.persistence.jpa.UserJpaRepository

    @Autowired
    private lateinit var hotelRepository: com.hotresvib.infrastructure.persistence.jpa.HotelJpaRepository

    @Autowired
    private lateinit var roomRepository: com.hotresvib.infrastructure.persistence.jpa.RoomJpaRepository

    @Autowired
    private lateinit var availabilityRepository: com.hotresvib.infrastructure.persistence.jpa.AvailabilityJpaRepository

    private var testUserId: UserId = UserId(UUID.randomUUID())
    private var testRoomId: RoomId = RoomId(UUID.randomUUID())

    @BeforeEach
    fun setup() {
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("lifecycle@test.com"),
            displayName = "Lifecycle User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )
        userRepository.save(user)
        testUserId = user.id

        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Lifecycle Hotel"),
            city = "Test City",
            country = "Test Country"
        )
        hotelRepository.save(hotel)

        val room = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = hotel.id,
            number = RoomNumber("201"),
            type = RoomType.SINGLE,
            baseRate = Money(BigDecimal("50.00"), "USD")
        )
        roomRepository.save(room)
        testRoomId = room.id

        val availability = Availability(
            id = AvailabilityId(UUID.randomUUID()),
            roomId = room.id,
            range = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)),
            available = AvailableQuantity(2)
        )
        availabilityRepository.save(availability)
    }

    @Test
    fun `expireReservation should accept id obtained from database`() {
        // create and move to pending payment
        val stay = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
        val reservation = reservationService.createReservation(
            userId = testUserId,
            roomId = testRoomId,
            stay = stay
        )

        val pending = reservationService.initiatePayment(reservation.id)
        assertEquals(ReservationStatus.PENDING_PAYMENT, pending.status)

        // expire using the same id object read from DB via service
        val expired = reservationService.expireReservation(pending.id)
        assertEquals(ReservationStatus.EXPIRED, expired.status)

        // ensure repository still able to load by id after expiration
        val found = reservationRepository.findById(expired.id)
        assertNotNull(found)
        assertEquals(ReservationStatus.EXPIRED, found?.status)
    }
}
