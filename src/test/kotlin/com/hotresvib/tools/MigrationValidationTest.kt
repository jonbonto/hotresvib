package com.hotresvib.tools

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.PostgreSQLContainer

class MigrationValidationTest {

    @Test
    fun `h2 demo migrations apply cleanly`() {
        val url = "jdbc:h2:mem:migration_test;DB_CLOSE_DELAY=-1"
        val user = "sa"
        val pw = ""

        val flyway = Flyway.configure()
            .dataSource(url, user, pw)
            .locations("classpath:db/migration-demo")
            .load()

        val result = flyway.migrate()
        assertTrue(result.migrationsExecuted > 0, "Expected at least one demo migration to be applied")
    }

    @Test
    fun `postgres migrations apply on Testcontainers`() {
        PostgreSQLContainer<Nothing>("postgres:15-alpine").use { pg ->
            pg.start()

            val url = pg.jdbcUrl
            val user = pg.username
            val pw = pg.password

            val flyway = Flyway.configure()
                .dataSource(url, user, pw)
                .locations("classpath:db/migration")
                .load()

            val result = flyway.migrate()
            assertTrue(result.migrationsExecuted > 0, "Expected at least one production migration to be applied")
        }
    }
}
