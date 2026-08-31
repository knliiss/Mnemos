package com.mnemos.telegram.bot.application.bet.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

enum class TelegramBetSide {
    YES,
    NO,
}

data class TelegramEventBet(
    val id: UUID,
    val roundId: UUID,
    val side: TelegramBetSide,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: String,
)

data class TelegramEventBetRound(
    val id: UUID,
    val eventType: String,
    val scheduledAt: Instant,
    val closesAt: Instant,
    val resolvesAt: Instant,
    val yesOdds: BigDecimal,
    val noOdds: BigDecimal,
    val status: String,
    val outcome: String?,
    val myBet: TelegramEventBet?,
)

sealed interface TelegramBetRoundsResult {
    data class Available(
        val rounds: List<TelegramEventBetRound>,
    ) : TelegramBetRoundsResult

    data object AccountRequired : TelegramBetRoundsResult

    data object Unavailable : TelegramBetRoundsResult
}

sealed interface TelegramBetHistoryResult {
    data class Available(
        val bets: List<TelegramEventBet>,
    ) : TelegramBetHistoryResult

    data object AccountRequired : TelegramBetHistoryResult

    data object Unavailable : TelegramBetHistoryResult
}

enum class TelegramBetMutationResult {
    ACCEPTED,
    ACCOUNT_REQUIRED,
    INVALID,
    UNAVAILABLE,
}

enum class BetMenuMode {
    ROUNDS,
    HISTORY,
}

enum class BetMenuFeedback {
    ACCEPTED,
    INVALID_AMOUNT,
    ACCOUNT_REQUIRED,
    UNAVAILABLE,
}

data class BetMenuScreenData(
    val mode: BetMenuMode,
    val rounds: TelegramBetRoundsResult,
    val history: TelegramBetHistoryResult?,
    val selectedRoundId: UUID?,
    val selectedSide: TelegramBetSide?,
    val feedback: BetMenuFeedback?,
    val waitingForAmount: Boolean,
    val increasing: Boolean,
)
