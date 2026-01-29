package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.reservation.Reservation
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.ReservationId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.integration.DatabaseIntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Transactional
class ReservationJpaRepositoryTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var reservationRepository: ReservationJpaRepository

    @Autowired
    private lateinit var userRepository: UserJpaRepository

    @Autowired
    private lateinit var roomRepository: RoomJpaRepository

    @Autowired
    private lateinit var hotelRepository: HotelJpaRepository

    private var testUserId: UserId? = null
    private var testRoomId: RoomId? = null

    @BeforeEach
    fun setup() {
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("reservation@test.com"),
            displayName = "Reservation User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )
        userRepository.save(user)
        testUserId = user.id

        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Reservation Hotel"),
            city = "Test City",
            country = "Test Country"
        )
        hotelRepository.save(hotel)

        val room = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = hotel.id,
            number = RoomNumber("101"),
            type = RoomType.DOUBLE,
            baseRate = Money(BigDecimal("100.00"), "USD")
        )
        roomRepository.save(room)
        testRoomId = room.id
    }

    @Test
    fun `should save and find reservation by id`() {
        val reservation = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = testUserId!!,
            roomId = testRoomId!!,
            stay = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)),
            totalAmount = Money(BigDecimal("200.00"), "USD"),
            status = ReservationStatus.CONFIRMED,
            createdAt = Instant.now()
        )

        val saved = reservationRepository.save(reservation)
        
        val found = reservationRepository.findById(saved.id).orElse(null)
        
        assertNotNull(found)
        assertEquals(reservation.status, found.status)
        assertEquals(reservation.totalAmount.amount, found.totalAmount.amount)
    }

    @Test
    fun `should find reservations by user id`() {
        val reservation1 = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = testUserId!!,
            roomId = testRoomId!!,
            stay = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
            totalAmount = Money(BigDecimal("100.00"), "USD"),
            status = ReservationStatus.CONFIRMED,
            createdAt = Instant.now()
        )

        val reservation2 = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = testUserId!!,
            roomId = testRoomId!!,
            stay = DateRange(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)),
            totalAmount = Money(BigDecimal("200.00"), "USD"),
            status = ReservationStatus.PENDING,
            createdAt = Instant.now()
        )

        reservationRepository.save(reservation1)
        reservationRepository.save(reservation2)
        
        val found = reservationRepository.findByUserId(testUserId!!)
        
        assertTrue(found.size >= 2)
        assertTrue(found.any { it.status == ReservationStatus.CONFIRMED })
        assertTrue(found.any { it.status == ReservationStatus.PENDING })
    }

    @Test
    fun `should save reservation with different statuses`() {
        val pending = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = testUserId!!,
            roomId = testRoomId!!,
            stay = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
            totalAmount = Money(BigDecimal("100.00"), "USD"),
            status = ReservationStatus.PENDING,
            createdAt = Instant.now()
        )

        val cancelled = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = testUserId!!,
            roomId = testRoomId!!,
            stay = DateRange(LocalDate.now().plusDays(3), LocalDate.now().plusDays(4)),
            totalAmount = Money(BigDecimal("100.00"), "USD"),
            status = ReservationStatus.CANCELLED,
            createdAt = Instant.now()
        )

        val savedPending = reservationRepository.save(pending)
        val savedCancelled = reservationRepository.save(cancelled)

        assertEquals(ReservationStatus.PENDING, savedPending.status)
        assertEquals(ReservationStatus.CANCELLED, savedCancelled.status)
    }

    @Test
    fun `should delete reservation by id`() {
        val reservation = Reservation(
            id = ReservationId(UUID.randomUUID()),
            userId = testUserId!!,
            roomId = testRoomId!!,
            stay = DateRange(LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)),
            totalAmount = Money(BigDecimal("100.00"), "USD"),
            status = ReservationStatus.CONFIRMED,
            createdAt = Instant.now()
        )

        val saved = reservationRepository.save(reservation)
        assertTrue(reservationRepository.existsById(saved.id))

        reservationRepository.deleteById(saved.id)
        
        assertFalse(reservationRepository.existsById(saved.id))
    }
}
