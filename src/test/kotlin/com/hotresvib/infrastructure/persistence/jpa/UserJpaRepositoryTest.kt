package com.hotresvib.infrastructure.persistence.jpa

import com.hotresvib.domain.shared.UserId
import com.hotresvib.domain.user.EmailAddress
import com.hotresvib.domain.user.User
import com.hotresvib.domain.user.UserRole
import com.hotresvib.integration.DatabaseIntegrationTestBase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
class UserJpaRepositoryTest : DatabaseIntegrationTestBase() {

    @Autowired
    private lateinit var userRepository: UserJpaRepository

    @Test
    fun `should save and find user by id`() {
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("test@example.com"),
            displayName = "Test User",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed_password"
        )

        val saved = userRepository.save(user)
        
        val found = userRepository.findById(saved.id).orElse(null)
        
        assertNotNull(found)
        assertEquals(user.email.value, found.email.value)
        assertEquals(user.displayName, found.displayName)
        assertEquals(user.role, found.role)
    }

    @Test
    fun `should find user by email`() {
        val email = "findme@example.com"
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress(email),
            displayName = "Find Me",
            role = UserRole.ADMIN,
            passwordHash = "hashed"
        )

        userRepository.save(user)
        
        val found = userRepository.findByEmail(email)
        
        assertNotNull(found)
        assertEquals(email, found?.email?.value)
        assertEquals(UserRole.ADMIN, found?.role)
    }

    @Test
    fun `should enforce unique email constraint`() {
        val email = "unique@example.com"
        val user1 = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress(email),
            displayName = "User 1",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        userRepository.save(user1)
        userRepository.flush()

        val user2 = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress(email),
            displayName = "User 2",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        assertThrows(Exception::class.java) {
            userRepository.save(user2)
            userRepository.flush()
        }
    }

    @Test
    fun `should delete user by id`() {
        val user = User(
            id = UserId(UUID.randomUUID()),
            email = EmailAddress("delete@example.com"),
            displayName = "Delete Me",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed"
        )

        val saved = userRepository.save(user)
        assertTrue(userRepository.existsById(saved.id))

        userRepository.deleteById(saved.id)
        
        assertFalse(userRepository.existsById(saved.id))
    }

    @Test
    fun `should return null when user not found by email`() {
        val found = userRepository.findByEmail("nonexistent@example.com")
        assertNull(found)
    }
}
