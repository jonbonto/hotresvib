package com.hotresvib.application.port

import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByEmail(email: EmailAddress): User?
    fun save(user: User): User
}
