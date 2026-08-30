package com.mnemos.telegram.bot.adapter.out.reward

import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardClaimResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardRestoreResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardStateResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "telegram-bot-reward-service-client",
    url = "\${mnemos.clients.reward-service.url}",
    configuration = [RewardServiceFeignConfiguration::class],
    dismiss404 = true,
)
interface RewardServiceClient {
    @GetMapping("/internal/v1/telegram/daily/{telegramId}")
    fun getDailyState(
        @PathVariable telegramId: Long,
    ): TelegramDailyRewardStateResponse?

    @PostMapping("/internal/v1/telegram/daily/{telegramId}/claim")
    fun claimDaily(
        @PathVariable telegramId: Long,
    ): TelegramDailyRewardClaimResponse?

    @PostMapping("/internal/v1/telegram/daily/{telegramId}/restore")
    fun restoreDaily(
        @PathVariable telegramId: Long,
    ): TelegramDailyRewardRestoreResponse?
}
