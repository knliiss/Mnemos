package com.mnemos.telegram.bot.application.widget.accounts

import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.port.out.AccountWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
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

class AccountWidgetActionHandlerTest {
    private val snapshots = mock<AccountWidgetSnapshotQuery>()
    private val accounts = mock<AccountGateway>()
    private val publisher = mock<AccountWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = AccountWidgetActionHandler(snapshots, accounts, publisher, sessions, responder)
    private val currentId = UUID.randomUUID()
    private val otherId = UUID.randomUUID()

    @Test
    fun staleAccountContextCannotSwitchAccounts() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountWidgetSnapshotResult.Available(view(otherId)))
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:acct:select:$otherId"), session(currentId)),
        )
        verifyNoInteractions(accounts)
    }

    @Test
    fun switchingOnlyAcceptsVisibleEnabledAccounts() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountWidgetSnapshotResult.Available(view(currentId)))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:acct:select:" + UUID.randomUUID().toString()), session(currentId)),
        )
        verifyNoInteractions(accounts)
    }

    @Test
    fun successfulSwitchCallsDomainGatewayAndUpdatesSession() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountWidgetSnapshotResult.Available(view(currentId)),
            AccountWidgetSnapshotResult.Available(view(otherId)),
        )
        whenever(accounts.changeActiveAccount(11L, otherId)).thenReturn(TelegramAccountSwitchResult.Switched)
        whenever(publisher.state(any())).thenReturn("""{"active":"new"}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:acct:select:$otherId"), session(currentId)),
        )

        verify(accounts).changeActiveAccount(11L, otherId)
        verify(sessions).save(session(otherId))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun repeatedPageRequestWithAnOutdatedPageIsRejected() {
        whenever(snapshots.load(11L, 1)).thenReturn(AccountWidgetSnapshotResult.Available(view(currentId, page = 1)))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:acct:next:0"), session(currentId, page = 1)),
        )
        verifyNoInteractions(accounts)
    }

    private fun query(action: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, action, "data", true, false)

    private fun session(id: UUID, page: Int = 0) =
        TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNTS, id, page)

    private fun view(activeId: UUID, page: Int = 0) =
        AccountWidgetView(
            activeAccountId = activeId,
            accounts =
                listOf(
                    AccountWidgetEntry(currentId, "Main", true, 1, 5, activeId == currentId, true),
                    AccountWidgetEntry(otherId, "Secondary", false, 2, 5, activeId == otherId, true),
                ),
            totalAccounts = 2,
            page = page,
            totalPages = 2,
            coin = 200L,
            prime = 20L,
        )
}
