package com.mnemos.telegram.bot.adapter.out.settings

import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackListResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSelectionResponse
import com.mnemos.personalization.contracts.internal.v1.UpdateInternalTelegramNotificationPackSelectionRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

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

    @GetMapping("/internal/v1/users/telegram/{telegramId}/notification-packs")
    fun getNotificationPacks(
        @PathVariable telegramId: Long,
    ): InternalTelegramNotificationPackListResponse?

    @PutMapping("/internal/v1/users/telegram/{telegramId}/notification-pack-selection")
    fun updateNotificationPackSelection(
        @PathVariable telegramId: Long,
        @RequestBody request: UpdateInternalTelegramNotificationPackSelectionRequest,
    ): InternalTelegramNotificationPackSelectionResponse?
}
