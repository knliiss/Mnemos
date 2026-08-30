package com.mnemos.telegram.bot.adapter.out.settings

import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSelectionResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "telegram-bot-personalization-service-client",
    url = "\${mnemos.clients.personalization-service.url}",
    configuration = [PersonalizationServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface PersonalizationServiceClient {
    @GetMapping("/internal/v1/users/telegram/{telegramId}/notification-pack-selection")
    fun getNotificationPackSelection(
        @PathVariable telegramId: Long,
    ): InternalTelegramNotificationPackSelectionResponse?
}
