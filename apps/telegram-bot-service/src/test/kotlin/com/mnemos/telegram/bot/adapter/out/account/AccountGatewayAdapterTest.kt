package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteRequest
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteResponse
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInvitesResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountStatus
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountSummaryResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserIdentityResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserSettingsResponse
import com.mnemos.contracts.account.internal.v1.JoinInternalTelegramAccountRequest
import com.mnemos.contracts.account.internal.v1.UpdateInternalTelegramUserLocalizationRequest
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountMutationResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.util.UUID

class AccountGatewayAdapterTest {
    @Test
    fun `gateway maps Telegram account context`() {
        val accountId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val client = FakeAccountServiceClient(context = context(accountId))
        val gateway = AccountGatewayAdapter(client)

        val result = gateway.getContext(931458258) as TelegramAccountContextResult.Available
        val account = result.context.accounts.single()

        assertEquals(accountId, result.context.activeAccountId)
        assertEquals("knaliz", account.cristalixNickname)
        assertEquals(true, account.owner)
    }

    @Test
    fun `gateway maps successful account create and join`() {
        val accountId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val gateway = AccountGatewayAdapter(FakeAccountServiceClient(context = context(accountId)))

        assertSame(
            TelegramAccountMutationResult.Updated,
            gateway.createAccount(931458258, "knaliz"),
        )
        assertSame(
            TelegramAccountMutationResult.Updated,
            gateway.joinAccount(931458258, "invite-code"),
        )
    }

    @Test
    fun `gateway maps successful active account switch`() {
        val accountId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val gateway = AccountGatewayAdapter(FakeAccountServiceClient(context = context(accountId)))

        val result = gateway.changeActiveAccount(931458258, accountId)

        assertSame(TelegramAccountSwitchResult.Switched, result)
    }

    @Test
    fun `gateway treats missing Telegram user as registration required`() {
        val gateway = AccountGatewayAdapter(FakeAccountServiceClient(context = null))

        assertSame(TelegramAccountContextResult.RegistrationRequired, gateway.getContext(931458258))
        assertSame(
            TelegramAccountSwitchResult.RegistrationRequired,
            gateway.changeActiveAccount(931458258, UUID.randomUUID()),
        )
    }

    @Test
    fun `gateway reports unavailable service failures`() {
        val gateway = AccountGatewayAdapter(FailingAccountServiceClient())

        assertSame(TelegramAccountContextResult.Unavailable, gateway.getContext(931458258))
        assertSame(
            TelegramAccountMutationResult.Unavailable,
            gateway.createAccount(931458258, "knaliz"),
        )
        assertSame(
            TelegramAccountMutationResult.Unavailable,
            gateway.joinAccount(931458258, "invite-code"),
        )
        assertSame(
            TelegramAccountSwitchResult.Unavailable,
            gateway.changeActiveAccount(931458258, UUID.randomUUID()),
        )
    }

    private fun context(accountId: UUID): InternalTelegramAccountContextResponse =
        InternalTelegramAccountContextResponse(
            activeAccountId = accountId,
            accounts =
                listOf(
                    InternalTelegramAccountSummaryResponse(
                        accountId = accountId,
                        cristalixUuid = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        cristalixNickname = "knaliz",
                        memberCount = 1,
                        maxMembers = 5,
                        owner = true,
                        active = true,
                        status = InternalTelegramAccountStatus.ACTIVE,
                    ),
                ),
        )

    private class FakeAccountServiceClient(
        private val context: InternalTelegramAccountContextResponse?,
    ) : AccountServiceClient {
        override fun bootstrapUserIdentity(
            telegramId: Long,
            username: String?,
        ): InternalTelegramUserIdentityResponse =
            InternalTelegramUserIdentityResponse(
                userId = UUID.fromString("019c11d1-bb2a-7000-8000-000000000001"),
            )

        override fun getAccounts(telegramId: Long): InternalTelegramAccountContextResponse? = context

        override fun createAccount(
            telegramId: Long,
            request: CreateInternalTelegramAccountRequest,
        ): InternalTelegramAccountContextResponse? = context

        override fun joinAccount(
            telegramId: Long,
            request: JoinInternalTelegramAccountRequest,
        ): InternalTelegramAccountContextResponse? = context

        override fun changeActiveAccount(
            telegramId: Long,
            request: ChangeInternalTelegramActiveAccountRequest,
        ): InternalTelegramAccountContextResponse? = context

        override fun getAccountInvites(telegramId: Long): InternalTelegramAccountInvitesResponse? = null

        override fun createAccountInvite(
            telegramId: Long,
            request: CreateInternalTelegramAccountInviteRequest,
        ): CreateInternalTelegramAccountInviteResponse? = null

        override fun revokeAccountInvite(
            telegramId: Long,
            inviteId: UUID,
        ): ResponseEntity<Void>? = null

        override fun getUserSettings(telegramId: Long): InternalTelegramUserSettingsResponse? = null

        override fun updateLocalization(
            telegramId: Long,
            request: UpdateInternalTelegramUserLocalizationRequest,
        ): InternalTelegramUserSettingsResponse? = null
    }

    private class FailingAccountServiceClient : AccountServiceClient {
        override fun bootstrapUserIdentity(
            telegramId: Long,
            username: String?,
        ): InternalTelegramUserIdentityResponse = error("account unavailable")

        override fun getAccounts(telegramId: Long): InternalTelegramAccountContextResponse = error("account unavailable")

        override fun createAccount(
            telegramId: Long,
            request: CreateInternalTelegramAccountRequest,
        ): InternalTelegramAccountContextResponse = error("account unavailable")

        override fun joinAccount(
            telegramId: Long,
            request: JoinInternalTelegramAccountRequest,
        ): InternalTelegramAccountContextResponse = error("account unavailable")

        override fun changeActiveAccount(
            telegramId: Long,
            request: ChangeInternalTelegramActiveAccountRequest,
        ): InternalTelegramAccountContextResponse = error("account unavailable")

        override fun getAccountInvites(telegramId: Long): InternalTelegramAccountInvitesResponse = error("account unavailable")

        override fun createAccountInvite(
            telegramId: Long,
            request: CreateInternalTelegramAccountInviteRequest,
        ): CreateInternalTelegramAccountInviteResponse = error("account unavailable")

        override fun revokeAccountInvite(
            telegramId: Long,
            inviteId: UUID,
        ): ResponseEntity<Void> = error("account unavailable")

        override fun getUserSettings(telegramId: Long): InternalTelegramUserSettingsResponse = error("account unavailable")

        override fun updateLocalization(
            telegramId: Long,
            request: UpdateInternalTelegramUserLocalizationRequest,
        ): InternalTelegramUserSettingsResponse = error("account unavailable")
    }
}
