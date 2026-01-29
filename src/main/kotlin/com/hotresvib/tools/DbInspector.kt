package com.hotresvib.tools

import java.sql.DriverManager

fun main() {
    val url = "jdbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
    DriverManager.getConnection(url, "sa", "").use { conn ->
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank")
            var found = false
            while (rs.next()) {
                found = true
                val rank = rs.getInt("installed_rank")
                val version = rs.getString("version")
                val desc = rs.getString("description")
                val success = rs.getObject("success")
                println("$rank | $version | $desc | $success")
            }
            if (!found) println("No flyway_schema_history rows found")
        }
    }
}
