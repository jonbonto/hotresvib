package com.hotresvib.tools

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

/**
 * Simple utility to print bcrypt hashes for given passwords.
 * Run via Gradle task `generateBcrypt`.
 */
fun main() {
    val encoder = BCryptPasswordEncoder(12)
    val passwords = listOf("RahasiaCus", "admin123")
    passwords.forEach { pw ->
        println("PASSWORD=${pw} -> ${encoder.encode(pw)}")
    }
}
