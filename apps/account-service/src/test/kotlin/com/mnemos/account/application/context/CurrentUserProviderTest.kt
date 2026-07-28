package com.mnemos.account.application.context

import com.mnemos.account.domain.exception.UserNotFoundException
import com.mnemos.account.support.InMemoryUserRepository
import com.mnemos.account.support.SecurityContextTest
import com.mnemos.account.support.authenticate
import com.mnemos.account.support.currentUserProvider
import com.mnemos.account.support.user
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
