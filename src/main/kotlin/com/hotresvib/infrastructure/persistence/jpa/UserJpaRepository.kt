package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserJpaRepository : JpaRepository<User, UUID> {
    fun findByEmail(value: String): User?
    fun findByUnsubscribeToken(token: String): User?
}
