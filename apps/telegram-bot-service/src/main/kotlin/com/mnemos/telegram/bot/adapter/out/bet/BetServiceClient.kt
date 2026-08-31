package com.mnemos.telegram.bot.adapter.out.bet

import com.mnemos.telegram.bot.contracts.internal.bet.TelegramBetOperationResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramBetOperationStatusResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramEventBetRoundResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramIncreaseEventBetRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramIncreaseItemRaceRequest
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceParticipantResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramItemRaceResponse
import com.mnemos.telegram.bot.contracts.internal.bet.TelegramJoinItemRaceRequest
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
    fun eventHistory(
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

    @GetMapping("/internal/v1/telegram/bets/{telegramId}/item-races")
    fun listItemRaces(
        @PathVariable telegramId: Long,
    ): List<TelegramItemRaceResponse>

    @GetMapping("/internal/v1/telegram/bets/{telegramId}/item-races/history")
    fun itemRaceHistory(
        @PathVariable telegramId: Long,
    ): List<TelegramItemRaceParticipantResponse>

    @PostMapping("/internal/v1/telegram/bets/{telegramId}/item-races/{raceId}/join")
    fun joinItemRace(
        @PathVariable telegramId: Long,
        @PathVariable raceId: UUID,
        @RequestBody request: TelegramJoinItemRaceRequest,
    ): TelegramBetOperationResponse

    @PatchMapping("/internal/v1/telegram/bets/{telegramId}/item-races/{raceId}/increase")
    fun increaseItemRace(
        @PathVariable telegramId: Long,
        @PathVariable raceId: UUID,
        @RequestBody request: TelegramIncreaseItemRaceRequest,
    ): TelegramBetOperationResponse

    @GetMapping("/internal/v1/telegram/bets/{telegramId}/operations/{operationId}")
    fun getOperation(
        @PathVariable telegramId: Long,
        @PathVariable operationId: UUID,
    ): TelegramBetOperationStatusResponse
}
