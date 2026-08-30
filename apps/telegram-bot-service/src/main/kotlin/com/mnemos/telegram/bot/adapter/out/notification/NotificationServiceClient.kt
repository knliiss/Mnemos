package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

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

    @GetMapping("/internal/v1/telegram/subscriptions/{telegramId}")
    fun listSubscriptions(
        @PathVariable telegramId: Long,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): TelegramSubscriptionListResponse

    @DeleteMapping("/internal/v1/telegram/subscriptions/{telegramId}/{subscriptionId}")
    fun deleteSubscription(
        @PathVariable telegramId: Long,
        @PathVariable subscriptionId: UUID,
    )
}
