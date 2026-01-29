package com.hotresvib.integration

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Base class for database integration tests using Testcontainers
 * Provides a PostgreSQL container that is shared across all tests
 */
@SpringBootTest
@Testcontainers
abstract class DatabaseIntegrationTestBase {
    
    companion object {
        @Container
        val postgresContainer = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("hotresvib_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false)  // Don't reuse container to avoid migration conflicts
        
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.flyway.enabled") { "false" }
        }
    }
}
