package com.hotresvib.application.service

import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.user.User
import com.hotresvib.domain.shared.UserId
import org.springframework.stereotype.Service

@Service
class UserApplicationService(
    private val userRepository: UserRepository
) {
    fun findUserById(id: UserId): User? {
        return userRepository.findById(id)
    }
}
