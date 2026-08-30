package com.mnemos.notification.adapter.out.telegram

import com.mnemos.telegram.bot.contracts.internal.message.EditTelegramMessageRequest
import com.mnemos.telegram.bot.contracts.internal.message.SendTelegramMessageRequest
import com.mnemos.telegram.bot.contracts.internal.message.TelegramDeliveryResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "notification-telegram-bot-service-client",
    url = "\${mnemos.clients.telegram-bot-service.url}",
    configuration = [TelegramBotServiceFeignConfiguration::class],
)
interface TelegramBotServiceClient {
    @PostMapping("/internal/v1/messages")
    fun send(
        @RequestBody request: SendTelegramMessageRequest,
    ): TelegramDeliveryResponse

    @PutMapping("/internal/v1/messages")
    fun edit(
        @RequestBody request: EditTelegramMessageRequest,
    ): TelegramDeliveryResponse
}
