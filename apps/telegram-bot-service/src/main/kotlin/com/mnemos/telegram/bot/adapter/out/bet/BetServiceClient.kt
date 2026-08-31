package com.mnemos.telegram.bot.adapter.out.bet

import com.mnemos.telegram.bot.contracts.internal.bet.TelegramBetOperationResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetRoundResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramIncreaseEventBetRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramPlaceEventBetRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.UUID

@FeignClient(
    name = "telegram-bot-bet-service-client",
    url = "\${mnemos.clients.bet-service.url}",
    configuration = [BetServiceFeignConfiguration::class],
)
interface BetServiceClient {
    @GetMapping("/internal/v1/telegram/bets/{telegramId}/event-rounds")
    fun listRounds(
        @PathVariable telegramId: Long,
    ): List<TelegramEventBetRoundResponse>

    @GetMapping("/internal/v1/telegram/bets/{telegramId}/event-bets/history")
    fun history(
        @PathVariable telegramId: Long,
    ): List<TelegramEventBetResponse>

    @PostMapping("/internal/v1/telegram/bets/{telegramId}/event-rounds/{roundId}")
    fun place(
        @PathVariable telegramId: Long,
        @PathVariable roundId: UUID,
        @RequestBody request: TelegramPlaceEventBetRequest,
    ): TelegramBetOperationResponse

    @PatchMapping("/internal/v1/telegram/bets/{telegramId}/event-rounds/{roundId}/increase")
    fun increase(
        @PathVariable telegramId: Long,
        @PathVariable roundId: UUID,
        @RequestBody request: TelegramIncreaseEventBetRequest,
    ): TelegramBetOperationResponse
}
