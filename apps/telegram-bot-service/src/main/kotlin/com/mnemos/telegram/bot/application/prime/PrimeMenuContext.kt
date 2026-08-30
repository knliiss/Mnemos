package com.mnemos.telegram.bot.application.prime

import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseInvoice
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseNotice

object PrimeMenuContext {
    private const val INVOICE_URL = "prime.invoice.url"
    private const val INVOICE_PRIME_AMOUNT = "prime.invoice.prime"
    private const val INVOICE_STARS_AMOUNT = "prime.invoice.stars"
    private const val NOTICE = "prime.notice"

    fun invoice(invoice: PrimePurchaseInvoice): Map<String, String> =
        mapOf(
            INVOICE_URL to invoice.url,
            INVOICE_PRIME_AMOUNT to invoice.primeAmount.toString(),
            INVOICE_STARS_AMOUNT to invoice.starsAmount.toString(),
        )

    fun notice(notice: PrimePurchaseNotice): Map<String, String> =
        mapOf(NOTICE to notice.name)

    fun invoice(context: Map<String, String>): PrimePurchaseInvoice? {
        val url = context[INVOICE_URL]?.takeIf(String::isNotBlank) ?: return null
        val primeAmount = context[INVOICE_PRIME_AMOUNT]?.toLongOrNull()?.takeIf { value -> value > 0 } ?: return null
        val starsAmount = context[INVOICE_STARS_AMOUNT]?.toIntOrNull()?.takeIf { value -> value > 0 } ?: return null

        return PrimePurchaseInvoice(
            primeAmount = primeAmount,
            starsAmount = starsAmount,
            url = url,
        )
    }

    fun notice(context: Map<String, String>): PrimePurchaseNotice? =
        context[NOTICE]?.let { value ->
            PrimePurchaseNotice.entries.firstOrNull { notice -> notice.name == value }
        }

    fun clear(): Map<String, String> = emptyMap()
}
