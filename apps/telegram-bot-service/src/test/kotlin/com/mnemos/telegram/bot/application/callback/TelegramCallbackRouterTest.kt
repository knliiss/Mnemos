package com.mnemos.telegram.bot.application.callback

import com.mnemos.telegram.bot.application.command.help.HandleTelegramHelpService
import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class TelegramCallbackRouterTest {
    private val helpService = mock<HandleTelegramHelpService>()
    private val menuService = mock<TelegramMenuService>()

    @Test
    fun `help callback has priority over scoped callback prefixes`() {
        val scopedHandler = scopedHandler("help:")
        val router = router(scopedHandler)
        val context = callbackContext(TelegramHelpSection.ACCOUNTS.callbackData)
        whenever(
            helpService.switchSection(
                telegramId = TELEGRAM_ID,
                messageId = MESSAGE_ID,
                section = TelegramHelpSection.ACCOUNTS,
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(helpService).switchSection(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            section = TelegramHelpSection.ACCOUNTS,
        )
        verify(scopedHandler, never()).handle(context)
        verifyNoInteractions(menuService)
    }

    @Test
    fun `scoped callback preserves callback context and result`() {
        val scopedHandler = scopedHandler("prime:")
        val router = router(scopedHandler)
        val context = callbackContext("prime:buy:250")
        whenever(scopedHandler.handle(context)).thenReturn(TelegramDeliveryResult.Retryable)

        val result = router.route(context)

        assertEquals(TelegramDeliveryResult.Retryable, result)
        verify(scopedHandler).handle(context)
        verifyNoInteractions(helpService, menuService)
    }

    @Test
    fun `external menu callback opens fresh menu`() {
        val router = router()
        val context = callbackContext(TelegramMenuScreen.OPEN_CALLBACK_DATA)
        whenever(menuService.open(TELEGRAM_ID)).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).open(TELEGRAM_ID)
        verifyNoInteractions(helpService)
    }

    @Test
    fun `back callback delegates to menu back navigation`() {
        val router = router()
        val context = callbackContext(TelegramMenuScreen.BACK_CALLBACK_DATA)
        whenever(menuService.back(TELEGRAM_ID, MESSAGE_ID)).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).back(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
        )
        verifyNoInteractions(helpService)
    }

    @Test
    fun `menu callback delegates to selected screen`() {
        val router = router()
        val context = callbackContext(TelegramMenuScreen.NOTIFICATIONS.callbackData)
        whenever(
            menuService.navigate(
                telegramId = TELEGRAM_ID,
                messageId = MESSAGE_ID,
                screen = TelegramMenuScreen.NOTIFICATIONS,
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = router.route(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).navigate(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            screen = TelegramMenuScreen.NOTIFICATIONS,
        )
        verifyNoInteractions(helpService)
    }

    @Test
    fun `unknown callback is confirmed without side effects`() {
        val scopedHandler = scopedHandler("known:")
        val router = router(scopedHandler)

        val result = router.route(callbackContext("unknown:value"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(scopedHandler, never()).handle(org.mockito.kotlin.any())
        verifyNoInteractions(helpService, menuService)
    }

    @Test
    fun `duplicate scoped callback prefixes are rejected`() {
        val first = scopedHandler("duplicate:")
        val second = scopedHandler("duplicate:")

        assertThrows(IllegalArgumentException::class.java) {
            router(first, second)
        }
    }

    private fun router(vararg handlers: TelegramScopedCallbackHandler): TelegramCallbackRouter =
        TelegramCallbackRouter(
            helpService = helpService,
            menuService = menuService,
            scopedHandlers = handlers.toList(),
        )

    private fun scopedHandler(prefix: String): TelegramScopedCallbackHandler =
        mock {
            on { callbackPrefix }.thenReturn(prefix)
        }

    private fun callbackContext(data: String): TelegramCallbackContext =
        TelegramCallbackContext(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            callbackQueryId = CALLBACK_QUERY_ID,
            data = data,
        )

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val MESSAGE_ID = 42
        const val CALLBACK_QUERY_ID = "callback-query-42"
    }
}
