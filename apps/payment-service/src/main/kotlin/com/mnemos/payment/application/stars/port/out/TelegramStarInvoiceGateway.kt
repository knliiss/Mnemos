package com.mnemos.payment.application.stars.port.out

data class TelegramStarInvoice(
    val title: String,
    val description: String,
    val payload: String,
    val starsAmount: Int,
    val photoUrl: String?,
)

fun interface TelegramStarInvoiceGateway {
    fun createInvoice(invoice: TelegramStarInvoice): String
}
