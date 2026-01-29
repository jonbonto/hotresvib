package com.hotresvib.domain.hotel

import com.hotresvib.domain.shared.RoomId
import com.hotresvib.domain.shared.HotelId
import com.hotresvib.domain.shared.Money
import java.math.BigDecimal
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat

class HotelTest {

    @Test
    fun `should create valid hotel`() {
        val hotelId = HotelId.generate()
        val hotel = Hotel(
            id = hotelId,
            name = HotelName("Grand Hotel"),
            city = "New York",
            country = "USA"
        )

        assertThat(hotel.id).isEqualTo(hotelId)
        assertThat(hotel.name.value).isEqualTo("Grand Hotel")
        assertThat(hotel.city).isEqualTo("New York")
        assertThat(hotel.country).isEqualTo("USA")
    }

    @Test
    fun `should create valid room with base rate`() {
        val roomId = RoomId.generate()
        val hotelId = HotelId.generate()
        val baseRate = Money(BigDecimal("150.00"), "USD")
        
        val room = Room(
            id = roomId,
            hotelId = hotelId,
            number = RoomNumber("101"),
            type = RoomType.DOUBLE,
            baseRate = baseRate
        )

        assertThat(room.id).isEqualTo(roomId)
        assertThat(room.hotelId).isEqualTo(hotelId)
        assertThat(room.number.value).isEqualTo("101")
        assertThat(room.type).isEqualTo(RoomType.DOUBLE)
        assertThat(room.baseRate).isEqualTo(baseRate)
    }

    @Test
    fun `should reject blank hotel name`() {
        assertThrows<IllegalArgumentException> {
            HotelName("")
        }
    }

    @Test
    fun `should reject blank city`() {
        assertThrows<IllegalArgumentException> {
            Hotel(
                id = HotelId.generate(),
                name = HotelName("Grand Hotel"),
                city = "",
                country = "USA"
            )
        }
    }

    @Test
    fun `should reject blank country`() {
        assertThrows<IllegalArgumentException> {
            Hotel(
                id = HotelId.generate(),
                name = HotelName("Grand Hotel"),
                city = "New York",
                country = ""
            )
        }
    }
}
