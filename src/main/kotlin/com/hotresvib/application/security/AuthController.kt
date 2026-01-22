package com.hotresvib.application.security

import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.infrastructure.security.JwtTokenProvider
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AuthRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String
)
data class AuthResponse(val accessToken: String, val refreshToken: String)

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByEmail(EmailAddress(request.email))
            ?: return ResponseEntity.status(401).build()
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            return ResponseEntity.status(401).build()
        }

        val access = jwtTokenProvider.generateAccessToken(user)
        val refresh = jwtTokenProvider.generateRefreshToken(user)

        return ResponseEntity.ok(AuthResponse(access.value, refresh.value))
    }
}
