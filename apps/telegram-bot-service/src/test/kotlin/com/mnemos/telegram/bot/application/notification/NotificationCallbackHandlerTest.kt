package com.mnemos.telegram.bot.application.notification

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class NotificationCallbackHandlerTest {
    private val menuService = mock<TelegramMenuService>()
    private val subscriptionGateway = mock<NotificationSubscriptionGateway>()
    private val creationService = mock<NotificationSubscriptionCreationService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()

    private val handler =
        NotificationCallbackHandler(
            menuService = menuService,
            subscriptionGateway = subscriptionGateway,
            creationService = creationService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `queued raid callback cannot replace active drop flow`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(NotificationCreationMode.ITEM_DROP_LIST))

        val result = handler.handle(callback(NotificationCallbackData.RAID_EDIT))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService, subscriptionGateway, creationService)
    }

    @Test
    fun `queued create callback cannot reset active creation draft`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(NotificationCreationMode.ITEM_DROP))

        val result = handler.handle(callback(NotificationCallbackData.CREATE))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService, subscriptionGateway, creationService)
    }

    @Test
    fun `queued drops callback cannot leave raid editor`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(NotificationCreationMode.RAID))

        val result = handler.handle(callback(NotificationCallbackData.DROPS))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(menuService, subscriptionGateway, creationService)
    }

    @Test
    fun `drop creation can return to drop list`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(NotificationCreationMode.ITEM_DROP))
        whenever(
            menuService.updateScreenContext(
                telegramId = eq(TELEGRAM_ID),
                messageId = eq(MESSAGE_ID),
                expectedScreen = eq(TelegramMenuScreen.NOTIFICATIONS),
                update = any(),
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback(NotificationCallbackData.DROPS))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).updateScreenContext(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.NOTIFICATIONS),
            update = any(),
        )
        verifyNoInteractions(subscriptionGateway, creationService)
    }

    @Test
    fun `drop list can open creation draft`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(session(NotificationCreationMode.ITEM_DROP_LIST))
        whenever(
            menuService.updateScreenContext(
                telegramId = eq(TELEGRAM_ID),
                messageId = eq(MESSAGE_ID),
                expectedScreen = eq(TelegramMenuScreen.NOTIFICATIONS),
                update = any(),
            ),
        ).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(callback(NotificationCallbackData.CREATE))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(menuService).updateScreenContext(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.NOTIFICATIONS),
            update = any(),
        )
        verifyNoInteractions(subscriptionGateway, creationService)
    }

    private fun session(mode: NotificationCreationMode): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = MESSAGE_ID,
            currentScreen = TelegramMenuScreen.NOTIFICATIONS,
            screenContext =
                if (mode == NotificationCreationMode.LIST) {
                    emptyMap()
                } else {
                    NotificationMenuContext.selectMode(mode)
                },
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
