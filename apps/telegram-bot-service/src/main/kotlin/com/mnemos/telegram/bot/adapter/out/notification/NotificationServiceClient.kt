package com.mnemos.telegram.bot.adapter.out.notification

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "telegram-bot-notification-service-client",
    url = "\${mnemos.clients.notification-service.url}",
    configuration = [NotificationServiceFeignConfiguration::class],
)
interface NotificationServiceClient {
    @PostMapping("/internal/v1/telegram/recipients/{telegramId}/activation")
    fun activateRecipient(
        @PathVariable telegramId: Long,
    )
}
