package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
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

class AccountManagementWidgetActionHandlerTest {
    private val snapshots = mock<AccountManagementWidgetSnapshotQuery>()
    private val publisher = mock<AccountManagementWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = AccountManagementWidgetActionHandler(snapshots, publisher, sessions, responder)
    private val accountId = UUID.randomUUID()
    private val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, accountId)

    @Test
    fun activeAccountChangeBlocksPagedDataRefresh() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountManagementWidgetSnapshotResult.Available(view(UUID.randomUUID(), 0)),
        )
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:next:0"), session))
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun paginationUpdatesOnlyMessageBoundSession() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view(accountId, 0)))
        whenever(snapshots.load(11L, 1)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view(accountId, 1)))
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:next:0"), session))
        verify(sessions).save(session.copy(page = 1))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun staleExpectedPageIsRejected() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view(accountId, 0)))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:next:1"), session))
        verifyNoInteractions(publisher, sessions)
    }

    private fun query(data: String) = TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun view(id: UUID, page: Int) = AccountManagementWidgetView(
        accountId = id,
        nickname = "Kirito",
        page = page,
        pages = 2,
        totalMembers = 4,
        totalRoles = 4,
        members = emptyList(),
        roles = emptyList(),
    )
}
