package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.hotel.HotelName
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.integration.DatabaseIntegrationTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class HotelJpaRepositoryTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var hotelRepository: HotelJpaRepository

    @Test
    fun `should save and find hotel by id`() {
        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Grand Plaza"),
            city = "New York",
            country = "USA"
        )

        val saved = hotelRepository.save(hotel)
        
        val found = hotelRepository.findById(saved.id).orElse(null)
        
        assertNotNull(found)
        assertEquals(hotel.name.value, found.name.value)
        assertEquals(hotel.city, found.city)
        assertEquals(hotel.country, found.country)
    }

    @Test
    fun `should find all hotels`() {
        val hotel1 = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Hotel 1"),
            city = "Paris",
            country = "France"
        )
        
        val hotel2 = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Hotel 2"),
            city = "London",
            country = "UK"
        )

        hotelRepository.save(hotel1)
        hotelRepository.save(hotel2)
        
        val all = hotelRepository.findAll()
        
        assertTrue(all.size >= 2)
        assertTrue(all.any { it.name.value == "Hotel 1" })
        assertTrue(all.any { it.name.value == "Hotel 2" })
    }

    @Test
    fun `should delete hotel by id`() {
        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Delete Me"),
            city = "Tokyo",
            country = "Japan"
        )

        val saved = hotelRepository.save(hotel)
        assertTrue(hotelRepository.existsById(saved.id))

        hotelRepository.deleteById(saved.id)
        
        assertFalse(hotelRepository.existsById(saved.id))
    }

    @Test
    fun `should count hotels`() {
        val initialCount = hotelRepository.count()
        
        val hotel = Hotel(
            id = HotelId(UUID.randomUUID()),
            name = HotelName("Count Me"),
            city = "Berlin",
            country = "Germany"
        )
        hotelRepository.save(hotel)
        
        assertEquals(initialCount + 1, hotelRepository.count())
    }
}
