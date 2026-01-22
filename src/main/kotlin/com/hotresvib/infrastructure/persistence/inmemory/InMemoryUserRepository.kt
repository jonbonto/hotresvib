package com.hotresvib.infrastructure.persistence.inmemory

import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import java.util.concurrent.ConcurrentHashMap

class InMemoryUserRepository : UserRepository {
    private val storage = ConcurrentHashMap<UserId, User>()
    private val lock = Any()

    override fun findById(id: UserId): User? =
        synchronized(lock) {
            storage[id]
        }

    override fun findByEmail(email: EmailAddress): User? =
        synchronized(lock) {
            storage.values.firstOrNull { it.email == email }
        }

    override fun save(user: User): User {
        synchronized(lock) {
            storage[user.id] = user
        }
        return user
    }
}
