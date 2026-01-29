package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.auth.RefreshToken
import com.hotresvib.domain.shared.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshToken, UUID> {
    fun findByToken(token: String): RefreshToken?
    fun findByUserId(userId: UserId): List<RefreshToken>
    fun deleteByUserId(userId: UserId)
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now")
    fun deleteExpired(now: Instant): Int
}
