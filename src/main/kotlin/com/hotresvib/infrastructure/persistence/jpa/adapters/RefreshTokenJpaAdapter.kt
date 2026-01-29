package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.RefreshTokenRepository
import com.hotresvib.domain.auth.RefreshToken
import com.hotresvib.domain.shared.UserId
import com.hotresvib.infrastructure.persistence.jpa.RefreshTokenJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Repository
@Primary
class RefreshTokenJpaAdapter(
    private val jpaRepository: RefreshTokenJpaRepository
) : RefreshTokenRepository {
    
    override fun save(token: RefreshToken): RefreshToken {
        return jpaRepository.save(token)
    }
    
    override fun findByToken(token: String): RefreshToken? {
        return jpaRepository.findByToken(token)
    }
    
    override fun findByUserId(userId: UserId): List<RefreshToken> {
        return jpaRepository.findByUserId(userId)
    }
    
    @Transactional
    override fun deleteByUserId(userId: UserId) {
        jpaRepository.deleteByUserId(userId)
    }
    
    override fun deleteById(id: UUID) {
        jpaRepository.deleteById(id)
    }
    
    @Transactional
    override fun deleteExpired() {
        jpaRepository.deleteExpired(Instant.now())
    }
}
