package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateBoosterSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateDungeonSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateGlobalSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateItemDropSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateRaidSubscriptionRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramCreateSubscriptionResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @PostMapping("/internal/v1/telegram/subscriptions/{telegramId}/item-drop")
    fun createItemDropSubscription(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateItemDropSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse

    @PostMapping("/internal/v1/telegram/subscriptions/{telegramId}/booster")
    fun createBoosterSubscription(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateBoosterSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse

    @PostMapping("/internal/v1/telegram/subscriptions/{telegramId}/dungeon")
    fun createDungeonSubscription(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateDungeonSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse

    @PatchMapping("/internal/v1/telegram/subscriptions/{telegramId}/raid")
    fun updateRaidSubscription(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateRaidSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse

    @PostMapping("/internal/v1/telegram/subscriptions/{telegramId}/global")
    fun createGlobalSubscription(
        @PathVariable telegramId: Long,
        @RequestBody request: TelegramCreateGlobalSubscriptionRequest,
    ): TelegramCreateSubscriptionResponse

    @DeleteMapping("/internal/v1/telegram/subscriptions/{telegramId}/{subscriptionId}")
    fun deleteSubscription(
        @PathVariable telegramId: Long,
        @PathVariable subscriptionId: UUID,
    )
}
