package com.hotresvib.application.web

import com.hotresvib.infrastructure.persistence.jpa.ReservationJpaRepository
import com.hotresvib.infrastructure.persistence.jpa.UserJpaRepository
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/debug")
@Profile("demo")
class DemoDebugController(
    private val userRepository: UserJpaRepository,
    private val reservationRepository: ReservationJpaRepository
) {

    @GetMapping("/users")
    fun listUsers(): Any = try {
        userRepository.findAll().map { user ->
            mapOf(
                "id" to user.id.value,
                "email" to user.email.value,
                "displayName" to user.displayName,
                "role" to user.role.name,
                "passwordHash" to user.passwordHash
            )
        }
    } catch (e: Exception) {
        mapOf("error" to e.message)
    }

    @GetMapping("/reservations")
    fun listReservations(): Any = try {
        reservationRepository.findAll().map { r ->
            mapOf(
                "id" to r.id.value,
                "userId" to r.userId.value,
                "roomId" to r.roomId.value,
                "startDate" to r.stay.startDate,
                "endDate" to r.stay.endDate,
                "totalAmount" to r.totalAmount.amount,
                "currency" to r.totalAmount.currency,
                "status" to r.status.name,
                "createdAt" to r.createdAt.toString()
            )
        }
    } catch (e: Exception) {
        mapOf("error" to e.message)
    }
}
