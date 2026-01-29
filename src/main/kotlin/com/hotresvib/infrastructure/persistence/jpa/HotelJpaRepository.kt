package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.hotel.Hotel
import com.hotresvib.domain.shared.HotelId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface HotelJpaRepository : JpaRepository<Hotel, HotelId>
