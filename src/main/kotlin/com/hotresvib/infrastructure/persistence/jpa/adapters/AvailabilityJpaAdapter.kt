package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.AvailabilityRepository
import com.hotresvib.domain.availability.Availability
import com.hotresvib.domain.availability.AvailabilityId
import com.hotresvib.domain.shared.RoomId
import com.hotresvib.infrastructure.persistence.jpa.AvailabilityJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class AvailabilityJpaAdapter(
    private val jpaRepository: AvailabilityJpaRepository
) : AvailabilityRepository {
    
    override fun save(availability: Availability): Availability {
        return jpaRepository.save(availability)
    }
    
    override fun findByRoomId(roomId: RoomId): List<Availability> {
        return jpaRepository.findByRoomId(roomId)
    }
}
