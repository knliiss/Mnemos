package com.mnemos.telegram.bot.application.widget.wallet

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalance
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletTransactionGateway
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class OpenWalletWidgetServiceTest {
    private val accounts = mock<AccountGateway>()
    private val balances = mock<AccountBalanceGateway>()
    private val transactions = mock<WalletTransactionGateway>()
    private val publisher = mock<WalletWidgetPublisher>()
    private val service =
        OpenWalletWidgetService(
            TelegramWidgetProperties(enabled = true),
            accounts,
            balances,
            transactions,
            publisher,
        )

    @Test
    fun accountlessUserCannotRetrieveTransactionHistory() {
        whenever(accounts.getContext(11L)).thenReturn(TelegramAccountContextResult.RegistrationRequired)
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L, 0))

        val captured = argumentCaptor<WalletWidgetView>()
        verify(publisher).publish(eq(11L), captured.capture())
        assertEquals(WalletHistoryStatus.ACCOUNT_REQUIRED, captured.firstValue.historyStatus)
        verifyNoInteractions(transactions, balances)
    }

    @Test
    fun mismatchedHistoryAccountIdIsNeverPublished() {
        val account = UUID.randomUUID()
        whenever(accounts.getContext(11L)).thenReturn(
            TelegramAccountContextResult.Available(TelegramAccountContext(account, emptyList())),
        )
        whenever(transactions.list(11L, 0)).thenReturn(
            WalletTransactionResult.Available(
                WalletTransactionPage(
                    accountId = UUID.randomUUID(),
                    entries = listOf(WalletTransactionEntry("Secret", 1, "COIN", "2026-11-09 19:00")),
                    page = 0,
                    totalPages = 1,
                    totalItems = 1,
                ),
            ),
        )

        assertEquals(WalletWidgetSnapshotResult.Unavailable, service.load(11L, 0))
        verifyNoInteractions(publisher)
    }

    @Test
    fun forbiddenHistoryKeepsBalanceButNeverRevealsTransactions() {
        val account = UUID.randomUUID()
        whenever(accounts.getContext(11L)).thenReturn(
            TelegramAccountContextResult.Available(TelegramAccountContext(account, emptyList())),
        )
        whenever(balances.getBalances(11L)).thenReturn(
            TelegramAccountBalanceResult.Available(TelegramAccountBalance(coin = 240, prime = 18)),
        )
        whenever(transactions.list(11L, 0)).thenReturn(WalletTransactionResult.Forbidden)
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L, 0))

        val captured = argumentCaptor<WalletWidgetView>()
        verify(publisher).publish(eq(11L), captured.capture())
        assertEquals(WalletHistoryStatus.FORBIDDEN, captured.firstValue.historyStatus)
        assertEquals(0, captured.firstValue.entries.size)
        assertEquals(240L, captured.firstValue.coin)
        assertEquals(18L, captured.firstValue.prime)
    }
}
