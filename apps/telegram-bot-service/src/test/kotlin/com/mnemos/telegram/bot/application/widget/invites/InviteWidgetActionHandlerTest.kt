package com.mnemos.telegram.bot.application.widget.invites

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteMutationResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetMutationGuard
import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetPublisher
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

class InviteWidgetActionHandlerTest {
    private val snapshots = mock<InviteWidgetSnapshotQuery>()
    private val gateway = mock<AccountInviteGateway>()
    private val publisher = mock<InviteWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val guard = mock<InviteWidgetMutationGuard>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = InviteWidgetActionHandler(snapshots, gateway, publisher, sessions, guard, responder)
    private val accountId = UUID.randomUUID()
    private val inviteId = UUID.randomUUID()
    private val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.INVITES, accountId)

    @Test
    fun staleAccountContextDoesNotCreateAnInvite() {
        whenever(snapshots.load(11L, 0)).thenReturn(InviteWidgetSnapshotResult.Available(view(UUID.randomUUID())))
        whenever(responder.reply(eq("query"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:inv:create:ONE_DAY"), session))
        verifyNoInteractions(gateway, guard)
    }

    @Test
    fun createsAnInviteOnlyOncePerQueryIdUsingExistingGateway() {
        whenever(snapshots.load(11L, 0)).thenReturn(InviteWidgetSnapshotResult.Available(view()))
        whenever(guard.claimQuery(11L, "query")).thenReturn(true, false)
        whenever(gateway.createInvite(11L, TelegramAccountInviteExpiry.ONE_DAY.expiresInMinutes))
            .thenReturn(TelegramAccountInviteMutationResult.UPDATED)
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("query"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)
        whenever(responder.reply(eq("query"), eq("ALREADY"), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:inv:create:ONE_DAY"), session))
        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:inv:create:ONE_DAY"), session))

        verify(gateway).createInvite(11L, 1_440L)
        verify(sessions).save(session)
    }

    @Test
    fun revokeRequiresVisibleActiveInviteAndTwoStepConfirmation() {
        whenever(snapshots.load(11L, 0)).thenReturn(InviteWidgetSnapshotResult.Available(view()))
        whenever(guard.consumeRevocation(11L, 8, inviteId)).thenReturn(true)
        whenever(guard.claimQuery(11L, "query")).thenReturn(true)
        whenever(gateway.revokeInvite(11L, inviteId)).thenReturn(TelegramAccountInviteMutationResult.UPDATED)
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("query"), eq("CONFIRM"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)
        whenever(responder.reply(eq("query"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:inv:confirm:$inviteId"), session))
        verify(guard).requestRevocation(11L, 8, inviteId)
        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:inv:revoke:$inviteId"), session))
        verify(gateway).revokeInvite(11L, inviteId)
    }

    @Test
    fun revokeWithoutConfirmationNeverTouchesDomainGateway() {
        whenever(snapshots.load(11L, 0)).thenReturn(InviteWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("query"), eq("CONFIRM_REQUIRED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:inv:revoke:$inviteId"), session))
        verifyNoInteractions(gateway)
    }

    @Test
    fun cannotRevokeInviteOutsideCurrentPage() {
        whenever(snapshots.load(11L, 0)).thenReturn(InviteWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("query"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:inv:confirm:" + UUID.randomUUID()), session),
        )
        verifyNoInteractions(gateway, guard)
    }

    private fun query(action: String) =
        TelegramWidgetQueryContext("query", 11L, 11L, 8, action, "data", true, false)

    private fun view(id: UUID = accountId) =
        InviteWidgetView(
            accountId = id,
            accountName = "Kirito",
            invites = listOf(InviteWidgetItem(inviteId, "INV-1", TelegramAccountInviteStatus.ACTIVE, null)),
            totalInvites = 1,
            page = 0,
            totalPages = 1,
        )
}
