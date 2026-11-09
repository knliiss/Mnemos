package com.mnemos.telegram.bot.application.widget.query

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
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

class TelegramWidgetQueryRouterTest {
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = mock<TelegramWidgetActionHandler>()
    private val router: TelegramWidgetQueryRouter

    init {
        whenever(handler.screen).thenReturn(TelegramWidgetScreen.DASHBOARD)
        router = TelegramWidgetQueryRouter(sessions, responder, listOf(handler))
    }

    @Test
    fun refusesAnotherActorBeforeLoadingSessions() {
        whenever(responder.reply(eq("q"), eq("FORBIDDEN"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(query(actor = 12)))
        verifyNoInteractions(sessions)
    }

    @Test
    fun validQueryReachesTheCorrectActionHandler() {
        val session = TelegramWidgetSession(11, 8, TelegramWidgetScreen.DASHBOARD, null)
        val request = query()
        whenever(sessions.find(11, 8)).thenReturn(session)
        whenever(handler.handle(request, session)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(request))
        verify(handler).handle(request, session)
    }

    @Test
    fun staleSessionReturnsExpired() {
        whenever(responder.reply(eq("q"), eq("EXPIRED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(query()))
        verifyNoInteractions(handler)
    }

    private fun query(actor: Long = 11L) =
        TelegramWidgetQueryContext("q", 11L, actor, 8, "w1:refresh", "data", true, false)
}
