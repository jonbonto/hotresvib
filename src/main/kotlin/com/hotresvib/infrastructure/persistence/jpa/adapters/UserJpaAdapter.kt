package com.hotresvib.infrastructure.persistence.jpa.adapters

import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Repository

@Repository
@Primary
class UserJpaAdapter(private val repo: UserJpaRepository) : UserRepository {
    override fun findById(id: UserId): User? = repo.findById(id).orElse(null)

    override fun findByEmail(email: EmailAddress): User? = repo.findByEmail(email.value)

    override fun save(user: User): User = repo.save(user)
}
