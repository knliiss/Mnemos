package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class AccountBalanceGatewayAdapterTest {
    @Test
    fun `gateway maps account currencies`() {
        val client =
            FakeWalletServiceClient(
                balances =
                    listOf(
                        TelegramAccountBalanceResponse(Currency.COIN, 125),
                        TelegramAccountBalanceResponse(Currency.PRIME, 3),
                    ),
            )
        val gateway = AccountBalanceGatewayAdapter(client)

        val result = gateway.getBalances(931458258) as TelegramAccountBalanceResult.Available

        assertEquals(125, result.balance.coin)
        assertEquals(3, result.balance.prime)
    }

    @Test
    fun `gateway fills missing currencies with zero`() {
        val gateway =
            AccountBalanceGatewayAdapter(
                FakeWalletServiceClient(
                    balances = listOf(TelegramAccountBalanceResponse(Currency.COIN, 50)),
                ),
            )

        val result = gateway.getBalances(931458258) as TelegramAccountBalanceResult.Available

        assertEquals(50, result.balance.coin)
        assertEquals(0, result.balance.prime)
    }

    @Test
    fun `gateway maps missing active account to not required`() {
        val gateway = AccountBalanceGatewayAdapter(FakeWalletServiceClient(balances = null))

        assertSame(TelegramAccountBalanceResult.NotRequired, gateway.getBalances(931458258))
    }

    @Test
    fun `gateway degrades wallet failure without blocking account screen`() {
        val gateway = AccountBalanceGatewayAdapter(FailingWalletServiceClient())

        assertSame(TelegramAccountBalanceResult.Unavailable, gateway.getBalances(931458258))
    }

    private class FakeWalletServiceClient(
        private val balances: List<TelegramAccountBalanceResponse>?,
    ) : WalletServiceClient {
        override fun getActiveAccountBalances(telegramId: Long): List<TelegramAccountBalanceResponse>? = balances
    }

    private class FailingWalletServiceClient : WalletServiceClient {
        override fun getActiveAccountBalances(telegramId: Long): List<TelegramAccountBalanceResponse> = error("wallet unavailable")
    }
}
