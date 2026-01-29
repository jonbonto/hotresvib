package com.hotresvib.domain.user

import com.hotresvib.domain.shared.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.assertj.core.api.Assertions.assertThat

class UserTest {

    @Test
    fun `should create valid user with generated id`() {
        val userId = UserId.generate()
        val email = EmailAddress("user@example.com")
        val user = User(
            id = userId,
            email = email,
            displayName = "John Doe",
            role = UserRole.CUSTOMER,
            passwordHash = "hashed_password_123"
        )

        assertThat(user.id).isEqualTo(userId)
        assertThat(user.email).isEqualTo(email)
        assertThat(user.displayName).isEqualTo("John Doe")
        assertThat(user.role).isEqualTo(UserRole.CUSTOMER)
        assertThat(user.passwordHash).isEqualTo("hashed_password_123")
    }

    @Test
    fun `should create different users with different ids`() {
        val user1 = User(
            id = UserId.generate(),
            email = EmailAddress("user1@example.com"),
            displayName = "User One",
            role = UserRole.CUSTOMER,
            passwordHash = "hash1"
        )
        
        val user2 = User(
            id = UserId.generate(),
            email = EmailAddress("user2@example.com"),
            displayName = "User Two",
            role = UserRole.CUSTOMER,
            passwordHash = "hash2"
        )

        assertThat(user1.id.value).isNotEqualTo(user2.id.value)
    }

    @Test
    fun `should reject invalid email format`() {
        assertThrows<IllegalArgumentException> {
            EmailAddress("invalid-email")
        }
    }

    @Test
    fun `should reject blank display name`() {
        val userId = UserId.generate()
        assertThrows<IllegalArgumentException> {
            User(
                id = userId,
                email = EmailAddress("user@example.com"),
                displayName = "",
                role = UserRole.CUSTOMER,
                passwordHash = "hash"
            )
        }
    }
}
