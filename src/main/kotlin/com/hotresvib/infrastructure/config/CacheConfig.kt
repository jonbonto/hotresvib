package com.hotresvib.infrastructure.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory

/**
 * Redis Cache Configuration for Phase 12 Performance Optimization
 *
 * Enables Spring Cache with Redis backend.
 * TTL and expiration configured via application.yml.
 */
@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): RedisCacheManager {
        return RedisCacheManager.create(connectionFactory)
    }
}
