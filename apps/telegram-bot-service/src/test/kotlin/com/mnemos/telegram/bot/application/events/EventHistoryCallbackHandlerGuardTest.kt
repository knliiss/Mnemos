package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class EventHistoryCallbackHandlerGuardTest {
    private val menuService = mock<TelegramMenuService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()

    private val handler =
        EventHistoryCallbackHandler(
            menuService = menuService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `active search input ignores queued refresh callback`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(searchSession())

        val result = handler.handle(callback(EventHistoryCallbackData.REFRESH))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `active search input ignores repeated search callback`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(searchSession())

        val result = handler.handle(callback(EventHistoryCallbackData.SEARCH))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `active search input can still be cancelled`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(searchSession())
        whenever(
            menuService.cancelTextInput(
                telegramId = eq(TELEGRAM_ID),
                messageId = eq(MESSAGE_ID),
                expectedScreen = eq(TelegramMenuScreen.EVENTS),
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback(EventHistoryCallbackData.SEARCH_CANCEL))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).cancelTextInput(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.EVENTS),
        )
    }

    @Test
    fun `search cancel without active search input is ignored`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            searchSession(pendingInput = null),
        )

        val result = handler.handle(callback(EventHistoryCallbackData.SEARCH_CANCEL))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    private fun searchSession(pendingInput: TelegramPendingInput? = TelegramPendingInput.EVENT_SEARCH): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = MESSAGE_ID,
            currentScreen = TelegramMenuScreen.EVENTS,
            pendingInput = pendingInput,
        )

    private fun callback(data: String): TelegramCallbackContext =
        TelegramCallbackContext(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            callbackQueryId = "callback-query",
            data = data,
        )

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val MESSAGE_ID = 42
    }
}
