package com.mnemos.account.application.service.account

import com.mnemos.account.application.query.account.AccountQueryRepository
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class InternalTelegramAccountServiceTest {
    @Test
    fun `Telegram context loads memberships for resolved user`() {
        val telegramId = TelegramId(931458258)
        val activeAccountId = AccountId.generate()
        val user = createUser(activeAccountId)
        val account = accountSummary(activeAccountId, active = true)
        val userRepository = mock<UserRepository>()
        val accountQueryRepository = mock<AccountQueryRepository>()
        val changeActiveAccountService = mock<ChangeActiveAccountService>()

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)
        whenever(
            accountQueryRepository.findSummariesByUserId(
                userId = user.id,
                activeAccountId = activeAccountId,
            ),
        ).thenReturn(listOf(account))

        val service =
            InternalTelegramAccountService(
                userRepository = userRepository,
                accountQueryRepository = accountQueryRepository,
                changeActiveAccountService = changeActiveAccountService,
            )

        val context = service.getContext(telegramId)

        assertNotNull(context)
        assertEquals(activeAccountId, context?.activeAccountId)
        assertEquals(listOf(account), context?.accounts)
    }

    @Test
    fun `Telegram switch delegates to existing active account policy`() {
        val telegramId = TelegramId(931458258)
        val targetAccountId = AccountId.generate()
        val user = createUser(activeAccountId = null)
        val userRepository = mock<UserRepository>()
        val accountQueryRepository = mock<AccountQueryRepository>()
        val changeActiveAccountService = mock<ChangeActiveAccountService>()

        whenever(userRepository.findByTelegramId(telegramId)).thenReturn(user)
        whenever(
            accountQueryRepository.findSummariesByUserId(
                userId = user.id,
                activeAccountId = user.activeAccountId,
            ),
        ).thenReturn(emptyList())

        val service =
            InternalTelegramAccountService(
                userRepository = userRepository,
                accountQueryRepository = accountQueryRepository,
                changeActiveAccountService = changeActiveAccountService,
            )

        service.changeActiveAccount(
            telegramId = telegramId,
            accountId = targetAccountId,
        )

        verify(changeActiveAccountService).changeActiveAccount(
            user = user,
            accountId = targetAccountId,
        )
    }

    private fun createUser(activeAccountId: AccountId?): User {
        val user =
            User.create(
                telegramId = TelegramId(931458258),
                username = Username("knaliz"),
                keycloakId = KeycloakId(UUID.fromString("39f20154-d86d-41c5-9659-6986437905bd")),
            )

        activeAccountId?.let(user::changeActiveAccount)

        return user
    }

    private fun accountSummary(
        accountId: AccountId,
        active: Boolean,
    ): AccountSummary =
        AccountSummary(
            id = accountId,
            cristalixUuid = UUID.fromString("11111111-1111-1111-1111-111111111111"),
            cristalixNickname = "knaliz",
            currentMemberId = AccountMemberId.generate(),
            memberCount = 1,
            maxMembers = 5,
            owner = true,
            active = active,
            status = AccountStatus.ACTIVE,
        )
}
