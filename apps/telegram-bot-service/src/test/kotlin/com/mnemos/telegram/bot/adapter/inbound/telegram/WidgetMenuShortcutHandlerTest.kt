package com.mnemos.telegram.bot.adapter.inbound.telegram

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class WidgetMenuShortcutHandlerTest {
    private val menuService = mock<TelegramMenuService>()
    private val handler = WidgetMenuShortcutHandler(menuService)

    @Test
    fun widgetShortcutOpensNewMenuIndependentlyOfMessageId() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback("w:open:notifications"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).open(
            telegramId = 101L,
            screen = TelegramMenuScreen.NOTIFICATIONS,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun widgetAccountManagementShortcutOpensExistingMenu() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(callback("w:open:account-manage")))
        verify(menuService).open(
            telegramId = 101L,
            screen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun onlineSearchShortcutUsesExistingMenu() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(callback("w:open:online")))
        verify(menuService).open(
            telegramId = 101L,
            screen = TelegramMenuScreen.ONLINE,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun invitesFallbackOpensExistingMenu() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)
        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(callback("w:open:account-invites")))
        verify(menuService).open(
            telegramId = 101L,
            screen = TelegramMenuScreen.ACCOUNT_INVITES,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun settingsFallbackOpensExistingMenu() {
        whenever(menuService.open(any(), any(), any(), isNull())).thenReturn(TelegramDeliveryResult.Delivered)
        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(callback("w:open:settings")))
        verify(menuService).open(
            telegramId = 101L,
            screen = TelegramMenuScreen.SETTINGS,
            screenContext = emptyMap(),
            pendingInput = null,
        )
    }

    @Test
    fun unknownWidgetShortcutIsIgnored() {
        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(callback("w:open:delete")))
        verifyNoInteractions(menuService)
    }

    private fun callback(data: String): TelegramCallbackContext =
        TelegramCallbackContext(
            telegramId = 101L,
            messageId = 404,
            callbackQueryId = "widget-1",
            data = data,
        )
}
