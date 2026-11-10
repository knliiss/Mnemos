package com.mnemos.telegram.bot.adapter.out.wallet

import com.mnemos.telegram.bot.application.widget.wallet.WalletTransactionResult
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountTransactionEntry
import com.mnemos.wallet.contracts.wallet.internal.TelegramAccountTransactionsResponse
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class WalletTransactionGatewayAdapterTest {
    private val client = mock<WalletServiceClient>()
    private val gateway = WalletTransactionGatewayAdapter(client)

    @Test
    fun `maps verified active account and capped history entries`() {
        val accountId = UUID.randomUUID()
        whenever(client.getActiveAccountTransactions(11L, 1, 4)).thenReturn(
            TelegramAccountTransactionsResponse(
                accountId = accountId,
                items =
                    listOf(
                        TelegramAccountTransactionEntry(
                            id = UUID.randomUUID(),
                            currency = Currency.COIN,
                            type = TransactionType.CREDIT,
                            source = TransactionSource.DAILY_REWARD,
                            amount = 50,
                            createdAt = Instant.parse("2026-11-09T19:30:00Z"),
                        ),
                    ),
                page = 1,
                totalPages = 3,
                totalItems = 10,
            ),
        )

        val result = gateway.list(11L, 1) as WalletTransactionResult.Available

        assertEquals(accountId, result.data.accountId)
        assertEquals(1, result.data.page)
        assertEquals(10L, result.data.totalItems)
        assertEquals(50L, result.data.entries.single().amount)
        assertTrue(result.data.entries.single().label.contains("DAILY REWARD"))
    }

    @Test
    fun `out of range page never calls the wallet service`() {
        assertEquals(WalletTransactionResult.Unavailable, gateway.list(11L, 1001))
        verifyNoInteractions(client)
    }
}
