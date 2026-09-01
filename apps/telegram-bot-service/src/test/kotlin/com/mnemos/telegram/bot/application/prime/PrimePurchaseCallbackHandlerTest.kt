package com.mnemos.telegram.bot.application.prime

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseCreationResult
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseInvoice
import com.mnemos.telegram.bot.application.prime.port.out.PrimePurchaseGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class PrimePurchaseCallbackHandlerTest {
    private val gateway = mock<PrimePurchaseGateway>()
    private val menuService = mock<TelegramMenuService>()
    private val sessionRepository = mock<TelegramMenuSessionRepository>()
    private val handler =
        PrimePurchaseCallbackHandler(
            gateway = gateway,
            menuService = menuService,
            sessionRepository = sessionRepository,
        )

    @Test
    fun `buy callback forwards callback query id as purchase request id`() {
        val invoice =
            PrimePurchaseInvoice(
                primeAmount = 250,
                starsAmount = 250,
                url = "https://t.me/invoice/example",
            )
        val context = callbackContext(PrimePurchaseCallbackData.buy(PACKAGE_ID))

        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(primeSession())
        whenever(
            gateway.createPurchase(
                telegramId = TELEGRAM_ID,
                packageId = PACKAGE_ID,
                requestId = CALLBACK_QUERY_ID,
            ),
        ).thenReturn(PrimePurchaseCreationResult.Ready(invoice))
        whenever(menuService.updateScreenContext(any(), any(), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(gateway).createPurchase(
            telegramId = TELEGRAM_ID,
            packageId = PACKAGE_ID,
            requestId = CALLBACK_QUERY_ID,
        )

        val updateCaptor = argumentCaptor<(Map<String, String>) -> Map<String, String>>()
        verify(menuService).updateScreenContext(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.PRIME),
            update = updateCaptor.capture(),
        )
        assertEquals(PrimeMenuContext.invoice(invoice), updateCaptor.firstValue(emptyMap()))
    }

    @Test
    fun `stale PRIME callback cannot create purchase`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            primeSession(messageId = MESSAGE_ID + 1),
        )

        val result = handler.handle(callbackContext(PrimePurchaseCallbackData.buy(PACKAGE_ID)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `callback from another screen cannot create purchase`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            primeSession(screen = TelegramMenuScreen.HOME),
        )

        val result = handler.handle(callbackContext(PrimePurchaseCallbackData.buy(PACKAGE_ID)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `queued buy callback cannot replace ready invoice`() {
        val invoice =
            PrimePurchaseInvoice(
                primeAmount = 250,
                starsAmount = 250,
                url = "https://t.me/invoice/existing",
            )
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(
            primeSession(screenContext = PrimeMenuContext.invoice(invoice)),
        )

        val result = handler.handle(callbackContext(PrimePurchaseCallbackData.buy(PACKAGE_ID)))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    @Test
    fun `refresh callback clears PRIME context without creating purchase`() {
        val context = callbackContext(PrimePurchaseCallbackData.REFRESH)

        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(primeSession())
        whenever(menuService.updateScreenContext(any(), any(), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(context)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(gateway, never()).createPurchase(any(), any(), any())

        val updateCaptor = argumentCaptor<(Map<String, String>) -> Map<String, String>>()
        verify(menuService).updateScreenContext(
            telegramId = eq(TELEGRAM_ID),
            messageId = eq(MESSAGE_ID),
            expectedScreen = eq(TelegramMenuScreen.PRIME),
            update = updateCaptor.capture(),
        )
        assertEquals(emptyMap<String, String>(), updateCaptor.firstValue(mapOf("old" to "value")))
    }

    @Test
    fun `unknown PRIME callback is confirmed without side effects`() {
        whenever(sessionRepository.find(TELEGRAM_ID)).thenReturn(primeSession())

        val result = handler.handle(callbackContext("prime:unknown"))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(gateway, menuService)
    }

    private fun callbackContext(data: String): TelegramCallbackContext =
        TelegramCallbackContext(
            telegramId = TELEGRAM_ID,
            messageId = MESSAGE_ID,
            callbackQueryId = CALLBACK_QUERY_ID,
            data = data,
        )

    private fun primeSession(
        messageId: Int = MESSAGE_ID,
        screen: TelegramMenuScreen = TelegramMenuScreen.PRIME,
        screenContext: Map<String, String> = emptyMap(),
    ): TelegramMenuSession =
        TelegramMenuSession(
            telegramId = TELEGRAM_ID,
            menuMessageId = messageId,
            currentScreen = screen,
            screenContext = screenContext,
        )

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val MESSAGE_ID = 42
        const val CALLBACK_QUERY_ID = "callback-query-42"
        const val PACKAGE_ID = "prime-250"
    }
}
