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

data class TelegramItemRacePosition(
    val id: UUID,
    val raceId: UUID,
    val accountNickname: String,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: String,
)

data class TelegramItemRace(
    val id: UUID,
    val itemName: String,
    val itemType: String,
    val registrationClosesAt: Instant,
    val startsAt: Instant,
    val expiresAt: Instant,
    val entryAmount: Long,
    val feePercent: Int,
    val status: String,
    val winnerAccountId: UUID?,
    val myPosition: TelegramItemRacePosition?,
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

sealed interface TelegramItemRacesResult {
    data class Available(
        val races: List<TelegramItemRace>,
    ) : TelegramItemRacesResult

    data object AccountRequired : TelegramItemRacesResult

    data object Unavailable : TelegramItemRacesResult
}

sealed interface TelegramItemRaceHistoryResult {
    data class Available(
        val positions: List<TelegramItemRacePosition>,
    ) : TelegramItemRaceHistoryResult

    data object AccountRequired : TelegramItemRaceHistoryResult

    data object Unavailable : TelegramItemRaceHistoryResult
}

sealed interface TelegramBetMutationResult {
    data class Accepted(
        val operationId: UUID,
    ) : TelegramBetMutationResult

    data object AccountRequired : TelegramBetMutationResult

    data object Invalid : TelegramBetMutationResult

    data object Unavailable : TelegramBetMutationResult
}

enum class TelegramBetOperationState {
    PENDING,
    COMPLETED,
    FAILED,
    UNAVAILABLE,
}

enum class BetMenuMode {
    ROUNDS,
    RACES,
    HISTORY,
}

enum class BetMenuFeedback {
    COMPLETED,
    FAILED,
    PENDING,
    INVALID_AMOUNT,
    ACCOUNT_REQUIRED,
    UNAVAILABLE,
}

data class BetMenuScreenData(
    val mode: BetMenuMode,
    val rounds: TelegramBetRoundsResult,
    val eventHistory: TelegramBetHistoryResult?,
    val races: TelegramItemRacesResult,
    val raceHistory: TelegramItemRaceHistoryResult?,
    val selectedRoundId: UUID?,
    val selectedRaceId: UUID?,
    val selectedSide: TelegramBetSide?,
    val feedback: BetMenuFeedback?,
    val waitingForAmount: Boolean,
    val increasing: Boolean,
)
