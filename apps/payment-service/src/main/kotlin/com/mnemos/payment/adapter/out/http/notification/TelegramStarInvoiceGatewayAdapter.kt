package com.mnemos.payment.adapter.out.http.notification

import com.mnemos.payment.application.stars.port.out.TelegramStarInvoice
import com.mnemos.payment.application.stars.port.out.TelegramStarInvoiceGateway
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceRequest
import org.springframework.stereotype.Component

@Component
class TelegramStarInvoiceGatewayAdapter(
    private val client: NotificationTelegramClient,
) : TelegramStarInvoiceGateway {
    override fun createInvoice(invoice: TelegramStarInvoice): String =
        client
            .createStarInvoice(
                CreateStarInvoiceRequest(
                    title = invoice.title,
                    description = invoice.description,
                    payload = invoice.payload,
                    starsAmount = invoice.starsAmount,
                    photoUrl = invoice.photoUrl,
                ),
            ).invoiceUrl
}
