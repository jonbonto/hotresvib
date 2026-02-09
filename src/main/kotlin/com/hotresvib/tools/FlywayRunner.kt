package com.hotresvib.tools

import org.flywaydb.core.Flyway

fun main() {
    val url = "jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    val user = "sa"
    val pw = ""

    val flyway = Flyway.configure()
        .dataSource(url, user, pw)
        // Use filesystem location to avoid picking up nested demo migrations which can
        // cause duplicate-version conflicts when both directories exist on the classpath
        .locations("filesystem:src/main/resources/db/migration")
        .load()

    val result = flyway.migrate()
    println("Migrations applied: ${result.migrationsExecuted}")

    val info = flyway.info().all()
    if (info.isEmpty()) {
        println("No migration info available")
    } else {
        println("installed_rank | version | description | success")
        info.forEach { r ->
            println("${r.installedRank} | ${r.version} | ${r.description} | ${r.state}")
        }
    }
}
