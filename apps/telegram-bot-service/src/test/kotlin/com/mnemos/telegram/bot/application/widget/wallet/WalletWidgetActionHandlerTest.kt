package com.mnemos.telegram.bot.application.widget.wallet

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletWidgetPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class WalletWidgetActionHandlerTest {
    private val snapshots = mock<WalletWidgetSnapshotQuery>()
    private val publisher = mock<WalletWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = WalletWidgetActionHandler(snapshots, publisher, sessions, responder)
    private val account = UUID.randomUUID()

    @Test
    fun oldWidgetCannotReadNewlySelectedAccountHistory() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            WalletWidgetSnapshotResult.Available(view(UUID.randomUUID(), 0)),
        )
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:wallet:refresh"), session()))
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun stalePageNavigationIsRejected() {
        whenever(snapshots.load(11L, 1)).thenReturn(
            WalletWidgetSnapshotResult.Available(view(account, 1)),
        )
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:wallet:next:0"), session(page = 1)),
        )
        verifyNoInteractions(publisher)
    }

    @Test
    fun validPagingUpdatesSessionAndPresentation() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            WalletWidgetSnapshotResult.Available(view(account, 0)),
        )
        whenever(snapshots.load(11L, 1)).thenReturn(
            WalletWidgetSnapshotResult.Available(view(account, 1)),
        )
        whenever(publisher.state(any())).thenReturn("""{"page":1}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:wallet:next:0"), session()),
        )

        verify(sessions).save(session().copy(page = 1))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    private fun query(action: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, action, "data", true, false)

    private fun session(page: Int = 0) =
        TelegramWidgetSession(11L, 8, TelegramWidgetScreen.WALLET, account, page)

    private fun view(accountId: UUID, page: Int) =
        WalletWidgetView(
            accountId = accountId,
            coin = 120,
            prime = 5,
            page = page,
            totalPages = 3,
            totalTransactions = 11,
            historyStatus = WalletHistoryStatus.AVAILABLE,
            entries = listOf(WalletTransactionEntry("Нагорода", 20, "COIN", "2026-11-09 18:00")),
        )
}
