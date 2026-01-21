package com.hotresvib.application.port

import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.User

interface UserRepository {
    fun findById(id: UserId): User?
    fun save(user: User): User
}
