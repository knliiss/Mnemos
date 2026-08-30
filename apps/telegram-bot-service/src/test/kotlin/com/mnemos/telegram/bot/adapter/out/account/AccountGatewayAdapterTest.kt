package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.ChangeInternalTelegramActiveAccountRequest
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountContextResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountStatus
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountSummaryResponse
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
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
        override fun getAccounts(telegramId: Long): InternalTelegramAccountContextResponse? = context

        override fun changeActiveAccount(
            telegramId: Long,
            request: ChangeInternalTelegramActiveAccountRequest,
        ): InternalTelegramAccountContextResponse? = context
    }

    private class FailingAccountServiceClient : AccountServiceClient {
        override fun getAccounts(telegramId: Long): InternalTelegramAccountContextResponse = error("account unavailable")

        override fun changeActiveAccount(
            telegramId: Long,
            request: ChangeInternalTelegramActiveAccountRequest,
        ): InternalTelegramAccountContextResponse = error("account unavailable")
    }
}
