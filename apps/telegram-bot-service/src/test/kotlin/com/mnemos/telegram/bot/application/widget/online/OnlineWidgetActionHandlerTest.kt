package com.mnemos.telegram.bot.application.widget.online

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.online.port.out.OnlineWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
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

class OnlineWidgetActionHandlerTest {
    private val snapshots = mock<OnlineWidgetSnapshotQuery>()
    private val publisher = mock<OnlineWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val textInput = mock<StartWidgetTextInputUseCase>()
    private val handler = OnlineWidgetActionHandler(snapshots, publisher, sessions, responder, textInput)

    @Test
    fun selectingUnlistedRealmIsRejected() {
        whenever(snapshots.load(11L, null)).thenReturn(OnlineWidgetSnapshotResult.Available(overview()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:on:realm:900"), session()))
        verifyNoInteractions(publisher)
    }

    @Test
    fun selectingVisibleRealmUpdatesSessionAndState() {
        whenever(snapshots.load(11L, null)).thenReturn(OnlineWidgetSnapshotResult.Available(overview()))
        whenever(snapshots.load(11L, 2)).thenReturn(OnlineWidgetSnapshotResult.Available(detail()))
        whenever(publisher.state(any())).thenReturn("""{"selected":2}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:on:realm:2"), session()))
        verify(sessions).save(session().copy(page = 2))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun backActionReturnsToRealmList() {
        whenever(snapshots.load(11L, 2)).thenReturn(OnlineWidgetSnapshotResult.Available(detail()))
        whenever(snapshots.load(11L, null)).thenReturn(OnlineWidgetSnapshotResult.Available(overview()))
        whenever(publisher.state(any())).thenReturn("""{"selected":null}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:on:list"), session(2)))
        verify(sessions).save(session())
    }

    @Test
    fun searchUsesReplyBoundInputWithoutLoadingOnlineSnapshot() {
        whenever(responder.reply(eq("q"), eq("INPUT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:on:search"), session()))
        verify(textInput).start(session(), WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH)
        verifyNoInteractions(snapshots, publisher)
    }

    @Test
    fun failedQueryAcknowledgementDoesNotOpenInput() {
        whenever(responder.reply(eq("q"), eq("INPUT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Retryable)

        assertEquals(TelegramDeliveryResult.Retryable, handler.handle(query("w1:on:search"), session()))
        verifyNoInteractions(textInput)
    }

    private fun query(data: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun session(realmNumber: Int = 0) =
        TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ONLINE, null, realmNumber)

    private fun overview() =
        OnlineWidgetView(120, 2, null, null, null, "19:00 UTC", listOf(OnlineWidgetRealm(2, "SAO-2", 40)), emptyList())

    private fun detail() =
        OnlineWidgetView(120, 2, 2, "SAO-2", 40, "19:00 UTC", emptyList(), listOf(OnlineWidgetPlayer("Kirito")))
}
