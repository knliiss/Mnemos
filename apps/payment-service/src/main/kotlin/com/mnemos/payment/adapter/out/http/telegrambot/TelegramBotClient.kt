package com.mnemos.payment.adapter.out.http.telegrambot

import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceRequest
import com.mnemos.payment.contracts.internal.telegram.CreateStarInvoiceResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "payment-telegram-bot-service-client",
    url = "\${mnemos.clients.telegram-bot-service.url}",
    configuration = [TelegramBotFeignConfiguration::class],
)
interface TelegramBotClient {
    @PostMapping("/internal/v1/telegram/stars/invoices")
    fun createStarInvoice(
        @RequestBody request: CreateStarInvoiceRequest,
    ): CreateStarInvoiceResponse
}
