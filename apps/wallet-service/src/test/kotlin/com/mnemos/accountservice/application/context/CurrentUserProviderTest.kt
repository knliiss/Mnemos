package com.mnemos.accountservice.application.context

import com.mnemos.accountservice.domain.exception.UserNotFoundException
import com.mnemos.accountservice.support.InMemoryUserRepository
import com.mnemos.accountservice.support.SecurityContextTest
import com.mnemos.accountservice.support.authenticate
import com.mnemos.accountservice.support.currentUserProvider
import com.mnemos.accountservice.support.user
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CurrentUserProviderTest : SecurityContextTest() {
    @Test
    fun `returns user matching authenticated keycloak identity`() {
        val user = user()
        val repository = InMemoryUserRepository(listOf(user))
        authenticate(user)

        assertEquals(user, currentUserProvider(repository).get())
    }

    @Test
    fun `throws when authenticated user is not initialized locally`() {
        val user = user()
        authenticate(user)

        assertThrows<UserNotFoundException> {
            currentUserProvider(InMemoryUserRepository()).get()
        }
    }
}
