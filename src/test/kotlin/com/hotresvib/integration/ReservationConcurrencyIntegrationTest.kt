package com.hotresvib.integration

import com.hotresvib.application.service.ReservationApplicationService
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.reservation.ReservationStatus
import com.hotresvib.domain.shared.DateRange
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.infrastructure.persistence.jpa.HotelJpaRepository
import com.hotresvib.infrastructure.persistence.jpa.ReservationJpaRepository
import com.hotresvib.infrastructure.persistence.jpa.RoomJpaRepository
import com.hotresvib.infrastructure.persistence.jpa.UserJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ReservationConcurrencyIntegrationTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var reservationService: ReservationApplicationService

    @Autowired
    private lateinit var userRepository: UserJpaRepository

    @Autowired
    private lateinit var hotelRepository: HotelJpaRepository

    @Autowired
    private lateinit var roomRepository: RoomJpaRepository

    @Autowired
    private lateinit var reservationRepository: ReservationJpaRepository

    private var roomId: RoomId = RoomId(UUID.randomUUID())
    private var userA: UserId = UserId(UUID.randomUUID())
    private var userB: UserId = UserId(UUID.randomUUID())

    @BeforeEach
    fun setup() {
        val hotel = hotelRepository.save(
            Hotel(
                id = HotelId(UUID.randomUUID()),
                name = HotelName("Concurrency Hotel"),
                city = "Test City",
                country = "Test Country"
            )
        )

        roomId = roomRepository.save(
            Room(
                id = RoomId(UUID.randomUUID()),
                hotelId = hotel.id,
                number = RoomNumber("701"),
                type = RoomType.SUITE,
                baseRate = Money(BigDecimal("250.00"), "USD")
            )
        ).id

        userA = userRepository.save(
            User(
                id = UserId(UUID.randomUUID()),
                email = EmailAddress("concurrency-a@test.com"),
                displayName = "Concurrency A",
                role = UserRole.CUSTOMER,
                passwordHash = "hashed"
            )
        ).id

        userB = userRepository.save(
            User(
                id = UserId(UUID.randomUUID()),
                email = EmailAddress("concurrency-b@test.com"),
                displayName = "Concurrency B",
                role = UserRole.CUSTOMER,
                passwordHash = "hashed"
            )
        ).id
    }

    @Test
    fun `concurrent pending-payment transitions allow exactly one success`() {
        val stay = DateRange(LocalDate.now().plusDays(5), LocalDate.now().plusDays(8))

        val reservationA = reservationService.createReservation(userA, roomId, stay)
        val reservationB = reservationService.createReservation(userB, roomId, stay)

        val readyLatch = CountDownLatch(2)
        val startLatch = CountDownLatch(1)
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        val executor = Executors.newFixedThreadPool(2)

        val runTransition = { reservationId: com.hotresvib.domain.shared.ReservationId ->
            readyLatch.countDown()
            startLatch.await(5, TimeUnit.SECONDS)
            try {
                reservationService.initiatePayment(reservationId)
                successCount.incrementAndGet()
            } catch (_: Exception) {
                failureCount.incrementAndGet()
            }
        }

        executor.submit { runTransition(reservationA.id) }
        executor.submit { runTransition(reservationB.id) }

        readyLatch.await(5, TimeUnit.SECONDS)
        startLatch.countDown()

        executor.shutdown()
        executor.awaitTermination(10, TimeUnit.SECONDS)

        assertThat(successCount.get()).isEqualTo(1)
        assertThat(failureCount.get()).isEqualTo(1)

        val statuses = listOfNotNull(
            reservationRepository.findById(reservationA.id.value).orElse(null)?.status,
            reservationRepository.findById(reservationB.id.value).orElse(null)?.status
        )

        assertThat(statuses.count { it == ReservationStatus.PENDING_PAYMENT }).isEqualTo(1)
        assertThat(statuses.count { it == ReservationStatus.DRAFT }).isEqualTo(1)
    }
}
