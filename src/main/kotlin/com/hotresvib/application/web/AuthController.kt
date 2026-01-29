package com.hotresvib.application.web

import com.hotresvib.application.dto.LoginRequest
import com.hotresvib.application.dto.LoginResponse
import com.hotresvib.application.dto.RegisterRequest
import com.hotresvib.application.dto.RegisterResponse
import com.hotresvib.infrastructure.security.JwtTokenProvider
import com.hotresvib.application.port.UserRepository
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.shared.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = ["*"])
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        return try {
            val user = userRepository.findByEmail(EmailAddress(request.email))
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null)

            if (!passwordEncoder.matches(request.password, user.passwordHash)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null)
            }

            val token = jwtTokenProvider.generateAccessToken(user).value
            ResponseEntity.ok(LoginResponse(
                token = token,
                userId = user.id.value,
                email = user.email.value,
                displayName = user.displayName
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
        }
    }

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<RegisterResponse> {
        return try {
            // Check if user already exists
            if (userRepository.findByEmail(EmailAddress(request.email)) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null)
            }

            val hashedPassword = passwordEncoder.encode(request.password)
            val newUser = User(
                id = UserId.generate(),
                email = EmailAddress(request.email),
                displayName = request.displayName,
                role = UserRole.CUSTOMER,
                passwordHash = hashedPassword
            )
            userRepository.save(newUser)

            ResponseEntity.status(HttpStatus.CREATED).body(RegisterResponse(
                userId = newUser.id.value,
                email = newUser.email.value,
                displayName = newUser.displayName,
                createdAt = Instant.now().toString()
            ))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
    }

    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") authHeader: String): ResponseEntity<Map<String, Any>> {
        return try {
            val token = authHeader.removePrefix("Bearer ").trim()
            val isValid = jwtTokenProvider.validateToken(token)
            if (isValid) {
                val userId = jwtTokenProvider.getSubjectFromToken(token)
                ResponseEntity.ok(mapOf("valid" to true, "userId" to (userId ?: "")))
            } else {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("valid" to false))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("valid" to false))
        }
    }
}
