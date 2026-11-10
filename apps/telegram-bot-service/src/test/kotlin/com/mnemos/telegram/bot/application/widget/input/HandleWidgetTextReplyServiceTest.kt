package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class HandleWidgetTextReplyServiceTest {
    private val pending = mock<WidgetTextInputSessionRepository>()
    private val widgets = mock<TelegramWidgetSessionRepository>()
    private val menu = mock<TelegramMenuService>()
    private val handler = HandleWidgetTextReplyService(TelegramWidgetProperties(enabled = true), pending, widgets, menu)

    @Test
    fun unrelatedReplyDoesNotInterceptLegacyInput() {
        assertNull(handler.handle(11L, 500, "Some message"))
        verifyNoInteractions(widgets, menu)
    }

    @Test
    fun eventReplyUsesExistingEventSearchAndConsumesPrompt() {
        givenPending(WidgetTextInputPurpose.EVENT_SEARCH, TelegramWidgetScreen.EVENTS)
        whenever(menu.open(eq(11L), eq(TelegramMenuScreen.EVENTS), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "  Kirito  "))
        verify(menu).open(11L, TelegramMenuScreen.EVENTS, EventHistoryMenuContext.search(emptyMap(), "Kirito"))
        verify(pending).delete(11L, 41)
    }

    @Test
    fun onlineReplyUsesExistingOnlinePlayerSearch() {
        givenPending(WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH, TelegramWidgetScreen.ONLINE)
        whenever(menu.open(eq(11L), eq(TelegramMenuScreen.ONLINE), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "Kirito"))
        verify(menu).open(11L, TelegramMenuScreen.ONLINE, SaoOnlineMenuContext.search("Kirito"))
        verify(pending).delete(11L, 41)
    }

    @Test
    fun retryableDeliveryPreservesPromptForTheNextUpdateAttempt() {
        givenPending(WidgetTextInputPurpose.EVENT_SEARCH, TelegramWidgetScreen.EVENTS)
        whenever(menu.open(eq(11L), eq(TelegramMenuScreen.EVENTS), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Retryable)

        assertEquals(TelegramDeliveryResult.Retryable, handler.handle(11L, 41, "test"))
        verify(pending, org.mockito.kotlin.never()).delete(any(), any())
    }

    @Test
    fun missingWidgetSessionDoesNotExecuteTheOldSearch() {
        whenever(pending.find(11L, 41)).thenReturn(
            WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.EVENT_SEARCH),
        )
        whenever(menu.open(eq(11L), eq(TelegramMenuScreen.EVENTS), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "private query"))
        verify(menu).open(11L, TelegramMenuScreen.EVENTS)
        verify(pending).delete(11L, 41)
    }

    private fun givenPending(purpose: WidgetTextInputPurpose, screen: TelegramWidgetScreen) {
        whenever(pending.find(11L, 41)).thenReturn(WidgetTextInputSession(11L, 41, 8, null, purpose))
        whenever(widgets.find(11L, 8)).thenReturn(TelegramWidgetSession(11L, 8, screen, null))
    }
}
