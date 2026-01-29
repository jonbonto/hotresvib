package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.user.User
import com.hotresvib.domain.shared.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<User, UserId> {
    fun findByEmail(value: String): User?
}
