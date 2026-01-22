package com.hotresvib.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "security.jwt")
data class JwtProperties(
    val secret: String
)

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtPropertiesConfig
