package com.mnemos.moderation.adapter.out.security

import com.mnemos.common.core.model.user.UserRole
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.security.MnemosPrincipal
import com.mnemos.security.exception.InvalidAuthenticationPrincipalException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class CurrentModeratorProviderAdapterTest {
    private val currentMnemosUser = mock<CurrentMnemosUser>()
    private val adapter = CurrentModeratorProviderAdapter(currentMnemosUser)

    @Test
    fun `missing Mnemos user id is returned as controlled authentication error`() {
        whenever(currentMnemosUser.get()).thenReturn(
            MnemosPrincipal(
                keycloakId = UUID.fromString("019c11d1-bb2a-7000-8000-000000000501"),
                username = "admin",
                telegramId = 910484582,
                userId = null,
                roles = setOf(UserRole.ADMIN),
            ),
        )
        whenever(currentMnemosUser.requireUserId()).thenThrow(
            InvalidAuthenticationPrincipalException(
                message = "Required JWT claim mnemos_user_id is missing",
            ),
        )

        assertThrows(InvalidAuthenticationPrincipalException::class.java) {
            adapter.current()
        }
    }
}
