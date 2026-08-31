package com.mnemos.telegram.bot.application.bet.port.out

import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetMutationResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import java.util.UUID

interface BetGateway {
    fun listRounds(telegramId: Long): TelegramBetRoundsResult

    fun history(telegramId: Long): TelegramBetHistoryResult

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
}
