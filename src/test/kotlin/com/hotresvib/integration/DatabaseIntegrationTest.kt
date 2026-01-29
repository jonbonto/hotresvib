package com.hotresvib.integration

import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.*
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.infrastructure.persistence.jpa.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Integration test for complete database operations
 * Tests the full stack from entities to database with PostgreSQL container
 */
@Transactional
class DatabaseIntegrationTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var userRepository: UserJpaRepository

    @Autowired
    private lateinit var hotelRepository: HotelJpaRepository

    @Autowired
    private lateinit var roomRepository: RoomJpaRepository

    @Autowired
    private lateinit var reservationRepository: ReservationJpaRepository

    @Autowired
    private lateinit var availabilityRepository: AvailabilityJpaRepository

    @Autowired
    private lateinit var pricingRuleRepository: PricingRuleJpaRepository

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenJpaRepository

    @Test
    fun `should perform complete booking flow from user to reservation`() {
        // 1. Create user
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("customer@test.com"),
            displayName = "John Doe",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed_password"
        )
        val savedUser = userRepository.save(user)
        assertNotNull(userRepository.findById(savedUser.id).orElse(null))

        // 2. Create hotel
        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Paradise Resort"),
            city = "Miami",
            country = "USA"
        )
        val savedHotel = hotelRepository.save(hotel)
        assertNotNull(hotelRepository.findById(savedHotel.id).orElse(null))

        // 3. Create room
        val room = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = savedHotel.id,
            number = RoomNumber("501"),
            type = RoomType.SUITE,
            baseRate = Money(BigDecimal("250.00"), "USD")
        )
        val savedRoom = roomRepository.save(room)
        assertNotNull(roomRepository.findById(savedRoom.id).orElse(null))

        // 4. Set availability
        val availability = com.hotresvib.domain.availability.Availability(
            id = com.hotresvib.domain.availability.AvailabilityId.generate(),
            roomId = savedRoom.id,
            range = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)),
            available = com.hotresvib.domain.availability.AvailableQuantity(5)
        )
        val savedAvailability = availabilityRepository.save(availability)
        assertNotNull(availabilityRepository.findById(savedAvailability.id).orElse(null))

        // 5. Create reservation
        val reservation = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = savedUser.id,
            roomId = savedRoom.id,
            stay = DateRange(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5)),
            totalAmount = Money(BigDecimal("750.00"), "USD"),
            status = ReservationStatus.CONFIRMED,
            createdAt = Instant.now()
        )
        val savedReservation = reservationRepository.save(reservation)
        assertNotNull(reservationRepository.findById(savedReservation.id).orElse(null))

        // 6. Verify relationships
        val userReservations = reservationRepository.findByUserId(savedUser.id)
        assertEquals(1, userReservations.size)
        assertEquals(savedReservation.id, userReservations[0].id)

        val hotelRooms = roomRepository.findByHotelId(savedHotel.id)
        assertTrue(hotelRooms.isNotEmpty())
        assertEquals(savedRoom.id, hotelRooms[0].id)

        val roomAvailability = availabilityRepository.findByRoomId(savedRoom.id)
        assertTrue(roomAvailability.isNotEmpty())
    }

    @Test
    fun `should handle multiple users and reservations`() {
        // Create hotel and room first
        val hotel = hotelRepository.save(
            Hotel(
                id = HotelId(UUID.randomUUID()),
                name = HotelName("Test Hotel"),
                city = "London",
                country = "UK"
            )
        )

        val room = roomRepository.save(
            Room(
                id = RoomId(UUID.randomUUID()),
                hotelId = hotel.id,
                number = RoomNumber("101"),
                type = RoomType.DOUBLE,
                baseRate = Money(BigDecimal("150.00"), "GBP")
            )
        )

        // Create multiple users
        val user1 = userRepository.save(
            User(
                id = UserId(UUID.randomUUID()),
                email = EmailAddress("user1@test.com"),
                displayName = "User 1",
                role = UserRole.CUSTOMER,
                passwordHash = "hash1"
            )
        )

        val user2 = userRepository.save(
            User(
                id = UserId(UUID.randomUUID()),
                email = EmailAddress("user2@test.com"),
                displayName = "User 2",
                role = UserRole.CUSTOMER,
                passwordHash = "hash2"
            )
        )

        // Create reservations for both users
        val reservation1 = reservationRepository.save(
            Reservation(
                id = ReservationId(UUID.randomUUID()),
                userId = user1.id,
                roomId = room.id,
                stay = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)),
                totalAmount = Money(BigDecimal("300.00"), "GBP"),
                status = ReservationStatus.CONFIRMED,
                createdAt = Instant.now()
            )
        )

        val reservation2 = reservationRepository.save(
            Reservation(
                id = ReservationId(UUID.randomUUID()),
                userId = user2.id,
                roomId = room.id,
                stay = DateRange(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)),
                totalAmount = Money(BigDecimal("300.00"), "GBP"),
                status = ReservationStatus.CONFIRMED,
                createdAt = Instant.now()
            )
        )

        // Verify each user has their reservation
        assertEquals(1, reservationRepository.findByUserId(user1.id).size)
        assertEquals(1, reservationRepository.findByUserId(user2.id).size)

        // Verify reservations exist
        assertNotNull(reservationRepository.findById(reservation1.id).orElse(null))
        assertNotNull(reservationRepository.findById(reservation2.id).orElse(null))
    }

    @Test
    fun `should handle refresh tokens correctly`() {
        // Create user
        val user = userRepository.save(
            User(
                id = UserId(UUID.randomUUID()),
                email = EmailAddress("tokenuser@test.com"),
                displayName = "Token User",
                role = UserRole.CUSTOMER,
                passwordHash = "hashed"
            )
        )

        // Create refresh token
        val refreshToken = com.hotresvib.domain.auth.RefreshToken.create(
            userId = user.id,
            token = UUID.randomUUID().toString()
        )

        val saved = refreshTokenRepository.save(refreshToken)

        // Find by token
        val foundByToken = refreshTokenRepository.findByToken(saved.token)
        assertNotNull(foundByToken)
        assertEquals(saved.id, foundByToken?.id)

        // Find by user ID
        val foundByUser = refreshTokenRepository.findByUserId(user.id)
        assertEquals(1, foundByUser.size)

        // Delete by user ID
        refreshTokenRepository.deleteByUserId(user.id)
        val afterDelete = refreshTokenRepository.findByUserId(user.id)
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `should verify all JPA converters work correctly`() {
        // Test UUID converters (UserId, HotelId, RoomId, ReservationId)
        val userId = UserId(UUID.randomUUID())
        val hotelId = HotelId(UUID.randomUUID())
        val roomId = RoomId(UUID.randomUUID())
        val reservationId = ReservationId(UUID.randomUUID())

        // Test String converters (EmailAddress, HotelName, RoomNumber)
        val email = EmailAddress("converter@test.com")
        val hotelName = HotelName("Converter Hotel")
        val roomNumber = RoomNumber("999")

        // Create entities using all value objects
        val user = userRepository.save(
            User(
                id = userId,
                email = email,
                displayName = "Converter Test",
                role = UserRole.ADMIN,
                passwordHash = "hashed"
            )
        )

        val hotel = hotelRepository.save(
            Hotel(
                id = hotelId,
                name = hotelName,
                city = "Test City",
                country = "Test Country"
            )
        )

        val room = roomRepository.save(
            Room(
                id = roomId,
                hotelId = hotelId,
                number = roomNumber,
                type = RoomType.SINGLE,
                baseRate = Money(BigDecimal("100.00"), "USD")
            )
        )

        // Verify all converters worked
        val foundUser = userRepository.findById(userId).orElse(null)
        assertNotNull(foundUser)
        assertEquals(email.value, foundUser.email.value)

        val foundHotel = hotelRepository.findById(hotelId).orElse(null)
        assertNotNull(foundHotel)
        assertEquals(hotelName.value, foundHotel.name.value)

        val foundRoom = roomRepository.findById(roomId).orElse(null)
        assertNotNull(foundRoom)
        assertEquals(roomNumber.value, foundRoom.number.value)
    }

    @Test
    fun `should verify database constraints work`() {
        // Try to save user with duplicate email
        val email = "duplicate@test.com"
        userRepository.save(
            User(
                id = UserId(UUID.randomUUID()),
                email = EmailAddress(email),
                displayName = "First User",
                role = UserRole.CUSTOMER,
                passwordHash = "hash"
            )
        )
        userRepository.flush()

        assertThrows(Exception::class.java) {
            userRepository.save(
                User(
                    id = UserId(UUID.randomUUID()),
                    email = EmailAddress(email),
                    displayName = "Second User",
                    role = UserRole.CUSTOMER,
                    passwordHash = "hash"
                )
            )
            userRepository.flush()
        }
    }
}
