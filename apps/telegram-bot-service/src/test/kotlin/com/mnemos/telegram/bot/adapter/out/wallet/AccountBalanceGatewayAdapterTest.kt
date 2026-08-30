package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountBalanceResponse
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.UUID

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

        val result = gateway.getBalances(UUID.randomUUID()) as TelegramAccountBalanceResult.Available

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

        val result = gateway.getBalances(UUID.randomUUID()) as TelegramAccountBalanceResult.Available

        assertEquals(50, result.balance.coin)
        assertEquals(0, result.balance.prime)
    }

    @Test
    fun `gateway degrades wallet failure without blocking account screen`() {
        val gateway = AccountBalanceGatewayAdapter(FailingWalletServiceClient())

        assertSame(TelegramAccountBalanceResult.Unavailable, gateway.getBalances(UUID.randomUUID()))
    }

    private class FakeWalletServiceClient(
        private val balances: List<TelegramAccountBalanceResponse>,
    ) : WalletServiceClient {
        override fun getAccountBalances(accountId: UUID): List<TelegramAccountBalanceResponse> = balances
    }

    private class FailingWalletServiceClient : WalletServiceClient {
        override fun getAccountBalances(accountId: UUID): List<TelegramAccountBalanceResponse> =
            error("wallet unavailable")
    }
}
