package com.mnemos.telegram.bot.application.online

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class SaoOnlineCallbackHandlerGuardTest {
    private val menuService = mock<TelegramMenuService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()

    private val handler =
        SaoOnlineCallbackHandler(
            menuService = menuService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `active player search ignores queued refresh callback`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(onlineSession())

        val result = handler.handle(callback(SaoOnlineCallbackData.REFRESH))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `active player search ignores queued realm callback`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(onlineSession())

        val result = handler.handle(callback(SaoOnlineCallbackData.realm(3)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `active player search can still be cancelled`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(onlineSession())
        whenever(
            menuService.cancelTextInput(
                telegramId = TELEGRAM_ID,
                messageId = MESSAGE_ID,
                expectedScreen = TelegramMenuScreen.ONLINE,
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback(SaoOnlineCallbackData.SEARCH_CANCEL))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).cancelTextInput(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            expectedScreen = TelegramMenuScreen.ONLINE,
        )
    }

    @Test
    fun `search cancel without active player search is ignored`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            onlineSession(pendingInput = null),
        )

        val result = handler.handle(callback(SaoOnlineCallbackData.SEARCH_CANCEL))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `stale online callback is ignored`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            onlineSession(messageId = MESSAGE_ID + 1),
        )

        val result = handler.handle(callback(SaoOnlineCallbackData.REFRESH))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService)
    }

    private fun onlineSession(
        pendingInput: TelegramPendingInput? = TelegramPendingInput.ONLINE_PLAYER_SEARCH,
        messageId: Int = MESSAGE_ID,
    ): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = messageId,
            currentScreen = TelegramMenuScreen.ONLINE,
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
