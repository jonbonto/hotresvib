package com.hotresvib.infrastructure.config

import com.hotresvib.application.port.UserRepository
import com.hotresvib.infrastructure.persistence.inmemory.InMemoryUserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RepositoryConfig {

    @Bean
    fun userRepository(): UserRepository = InMemoryUserRepository()
}
