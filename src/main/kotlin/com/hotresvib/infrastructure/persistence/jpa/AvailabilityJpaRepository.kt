package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.RoomId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AvailabilityJpaRepository : JpaRepository<Availability, UUID> {
    fun findByRoomId(roomId: RoomId): List<Availability>
}
