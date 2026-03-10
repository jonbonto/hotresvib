package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.shared.RoomId
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AvailabilityJpaRepository : JpaRepository<Availability, UUID> {
    fun findByRoomId(roomId: RoomId): List<Availability>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findByRoomIdAndIdIsNotNull(roomId: RoomId): List<Availability>
}
