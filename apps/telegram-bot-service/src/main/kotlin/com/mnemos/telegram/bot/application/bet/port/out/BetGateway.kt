package com.mnemos.telegram.bot.application.bet.port.out

import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetMutationResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetOperationState
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRaceHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import java.util.UUID

interface BetGateway {
    fun listRounds(telegramId: Long): TelegramBetRoundsResult

    fun eventHistory(telegramId: Long): TelegramBetHistoryResult

    fun listItemRaces(telegramId: Long): TelegramItemRacesResult

    fun itemRaceHistory(telegramId: Long): TelegramItemRaceHistoryResult

    fun getOperationState(
        telegramId: Long,
        operationId: UUID,
    ): TelegramBetOperationState

    fun place(
        telegramId: Long,
        roundId: UUID,
        side: TelegramBetSide,
        amount: Long,
        idempotencyKey: UUID,
    ): TelegramBetMutationResult

    fun increase(
        telegramId: Long,
        roundId: UUID,
        amount: Long,
        idempotencyKey: UUID,
    ): TelegramBetMutationResult

    fun joinItemRace(
        telegramId: Long,
        raceId: UUID,
        idempotencyKey: UUID,
    ): TelegramBetMutationResult

    fun increaseItemRace(
        telegramId: Long,
        raceId: UUID,
        amount: Long,
        idempotencyKey: UUID,
    ): TelegramBetMutationResult
}
