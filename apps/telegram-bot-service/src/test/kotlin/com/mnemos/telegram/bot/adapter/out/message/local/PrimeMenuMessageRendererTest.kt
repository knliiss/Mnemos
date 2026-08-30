package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
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
    fun `renderer exposes packages as scoped callbacks`() {
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
        assertTrue(message.text.contains("100 PRIME"))
    }

    @Test
    fun `renderer exposes invoice url only through url action`() {
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
        assertFalse(callbackData.any { data -> data.contains(invoiceUrl) })
        assertTrue(message.text.contains("Счёт готов"))
    }

    private fun primePackage(): PrimePurchasePackage =
        PrimePurchasePackage(
            id = "prime-100",
            primeAmount = 100,
            starsAmount = 100,
        )
}
