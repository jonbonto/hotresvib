package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.hotel.Room
import com.hotresvib.domain.hotel.RoomNumber
import com.hotresvib.domain.hotel.RoomType
import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.integration.DatabaseIntegrationTestBase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Transactional
class RoomJpaRepositoryTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var roomRepository: RoomJpaRepository

    @Autowired
    private lateinit var hotelRepository: HotelJpaRepository

    private var testHotelId: HotelId? = null

    @BeforeEach
    fun setup() {
        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Test Hotel"),
            city = "Test City",
            country = "Test Country"
        )
        hotelRepository.save(hotel)
        testHotelId = hotel.id
    }

    @Test
    fun `should save and find room by id`() {
        val room = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = testHotelId!!,
            number = RoomNumber("101"),
            type = RoomType.DOUBLE,
            baseRate = Money(BigDecimal("150.00"), "USD")
        )

        val saved = roomRepository.save(room)
        
        val found = roomRepository.findById(saved.id).orElse(null)
        
        assertNotNull(found)
        assertEquals(room.number.value, found.number.value)
        assertEquals(room.type, found.type)
        assertEquals(room.baseRate.amount, found.baseRate.amount)
        assertEquals(room.baseRate.currency, found.baseRate.currency)
    }

    @Test
    fun `should find rooms by hotel id`() {
        val room1 = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = testHotelId!!,
            number = RoomNumber("201"),
            type = RoomType.SINGLE,
            baseRate = Money(BigDecimal("100.00"), "USD")
        )
        
        val room2 = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = testHotelId!!,
            number = RoomNumber("202"),
            type = RoomType.SUITE,
            baseRate = Money(BigDecimal("250.00"), "USD")
        )

        roomRepository.save(room1)
        roomRepository.save(room2)
        
        val rooms = roomRepository.findByHotelId(testHotelId!!)
        
        assertTrue(rooms.size >= 2)
        assertTrue(rooms.any { it.number.value == "201" })
        assertTrue(rooms.any { it.number.value == "202" })
    }

    @Test
    fun `should save room with different types`() {
        val singleRoom = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = testHotelId!!,
            number = RoomNumber("301"),
            type = RoomType.SINGLE,
            baseRate = Money(BigDecimal("80.00"), "EUR")
        )

        val suiteRoom = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = testHotelId!!,
            number = RoomNumber("302"),
            type = RoomType.SUITE,
            baseRate = Money(BigDecimal("300.00"), "EUR")
        )

        val savedSingle = roomRepository.save(singleRoom)
        val savedSuite = roomRepository.save(suiteRoom)

        assertEquals(RoomType.SINGLE, savedSingle.type)
        assertEquals(RoomType.SUITE, savedSuite.type)
    }

    @Test
    fun `should delete room by id`() {
        val room = Room(
            id = RoomId(UUID.randomUUID()),
            hotelId = testHotelId!!,
            number = RoomNumber("401"),
            type = RoomType.DOUBLE,
            baseRate = Money(BigDecimal("120.00"), "USD")
        )

        val saved = roomRepository.save(room)
        assertTrue(roomRepository.existsById(saved.id))

        roomRepository.deleteById(saved.id)
        
        assertFalse(roomRepository.existsById(saved.id))
    }
}
