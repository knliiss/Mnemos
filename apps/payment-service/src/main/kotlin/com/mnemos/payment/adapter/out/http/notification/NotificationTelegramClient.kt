package com.mnemos.payment.adapter.out.http.notification

import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceRequest
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "payment-notification-telegram-client",
    url = "\${mnemos.clients.notification-service.url}",
    configuration = [NotificationTelegramFeignConfiguration::class],
)
interface NotificationTelegramClient {
    @PostMapping("/internal/v1/telegram/stars/invoices")
    fun createStarInvoice(
        @RequestBody request: CreateStarInvoiceRequest,
    ): CreateStarInvoiceResponse
}
