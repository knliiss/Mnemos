package com.mnemos.account.application.service.user

import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InternalActiveAccountUsersServiceTest {
    private val repository = mock<UserRepository>()
    private val service = InternalActiveAccountUsersService(repository)

    @Test
    fun `active account nickname lookup trims input and returns repository matches`() {
        val user = mock<User>()

        whenever(repository.findAllByActiveAccountNickname("Knaliz")).thenReturn(listOf(user))

        assertEquals(listOf(user), service.findByNickname("  Knaliz  "))
        verify(repository).findAllByActiveAccountNickname("Knaliz")
    }
}
