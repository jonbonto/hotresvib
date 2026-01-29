package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.shared.RoomId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface AvailabilityJpaRepository : JpaRepository<Availability, AvailabilityId> {
    fun findByRoomId(roomId: RoomId): List<Availability>
}
