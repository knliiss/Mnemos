package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.prime.PrimePurchaseCallbackData
import com.mnemos.telegram.bot.application.prime.model.PrimeMenuScreenData
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseInvoice
import com.mnemos.telegram.bot.application.prime.model.PrimePurchasePackage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrimeMenuMessageRendererTest {
    @Test
    fun `renderer exposes PRIME packages as full width account scoped actions`() {
        val message =
            PrimeMenuMessageRenderer.render(
                TelegramMenuScreenData.Prime(
                    PrimeMenuScreenData(
                        packages = listOf(primePackage()),
                        invoice = null,
                        notice = null,
                    ),
                ),
            )

        val buyAction =
            message.actions
                .filterIsInstance<OutboundTelegramMessageAction.Callback>()
                .first { action -> action.data.startsWith("prime:buy:") }

        assertEquals(PrimePurchaseCallbackData.buy("prime-100"), buyAction.data)
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, buyAction.style)
        assertTrue(message.text.contains("⭐ <b>PRIME и баланс</b>"))
        assertTrue(message.text.contains("└ <b>100 PRIME</b> · ⭐ 100"))
        assertTrue(message.actionRows.all { row -> row.size == 1 })
        assertTrue(
            message.actions
                .filterIsInstance<OutboundTelegramMessageAction.Callback>()
                .any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA },
        )
    }

    @Test
    fun `ready invoice hides package creation actions and focuses payment`() {
        val invoiceUrl = "https://t.me/invoice/private-token"
        val message =
            PrimeMenuMessageRenderer.render(
                TelegramMenuScreenData.Prime(
                    PrimeMenuScreenData(
                        packages = listOf(primePackage()),
                        invoice =
                            PrimePurchaseInvoice(
                                primeAmount = 100,
                                starsAmount = 100,
                                url = invoiceUrl,
                            ),
                        notice = null,
                    ),
                ),
            )

        val urlAction = message.actions.filterIsInstance<OutboundTelegramMessageAction.OpenUrl>().single()
        val callbackData =
            message.actions
                .filterIsInstance<OutboundTelegramMessageAction.Callback>()
                .map { action -> action.data }

        assertEquals(invoiceUrl, urlAction.url)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, urlAction.style)
        assertFalse(callbackData.any { data -> data.contains(invoiceUrl) })
        assertFalse(callbackData.any { data -> data.startsWith("prime:buy:") })
        assertFalse(message.text.contains("Пополнение через Telegram Stars"))
        assertTrue(message.text.contains("⭐ <b>Счёт готов</b>"))
        assertTrue(message.text.contains("Нажмите кнопку оплаты ниже"))
        assertEquals(listOf(1, 1, 1), message.actionRows.map(List<*>::size))
    }

    private fun primePackage(): PrimePurchasePackage =
        PrimePurchasePackage(
            id = "prime-100",
            primeAmount = 100,
            starsAmount = 100,
        )
}
