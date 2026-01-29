package com.hotresvib.application.dto

import java.util.UUID

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val userId: UUID,
    val email: String,
    val displayName: String
)

data class RegisterRequest(
    val email: String,
    val displayName: String,
    val password: String
)

data class RegisterResponse(
    val userId: UUID,
    val email: String,
    val displayName: String,
    val createdAt: String
)
