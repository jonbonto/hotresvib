package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository : UserRepository {
    private val storage = ConcurrentHashMap<UserId, User>()

    override fun findById(id: UserId): User? = storage[id]

    override fun findByEmail(email: EmailAddress): User? =
        storage.values.firstOrNull { it.email == email }

    override fun save(user: User): User {
        storage[user.id] = user
        return user
    }
}
