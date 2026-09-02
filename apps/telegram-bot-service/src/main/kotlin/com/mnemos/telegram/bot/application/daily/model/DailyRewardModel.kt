package com.mnemos.telegram.bot.application.daily.model

import java.time.LocalDate
import java.util.UUID

data class DailyRewardState(
    val accountId: UUID,
    val currentStreak: Int,
    val claimedToday: Boolean,
    val recoverable: Boolean,
    val recoverableStreak: Int,
    val restoresRemaining: Int,
    val nextCoinMin: Long,
    val nextCoinMax: Long,
    val primeChance: Double,
    val rewardDate: LocalDate,
)

data class DailyClaimedReward(
    val streak: Int,
    val coins: Long,
    val primes: Long,
)

data class DailyRestoredStreak(
    val currentStreak: Int,
)

sealed interface DailyRewardStateResult {
    data class Available(
        val state: DailyRewardState,
    ) : DailyRewardStateResult

    data object AccountRequired : DailyRewardStateResult

    data object Unavailable : DailyRewardStateResult
}

sealed interface DailyClaimResult {
    data class Claimed(
        val reward: DailyClaimedReward,
    ) : DailyClaimResult

    data object AccountRequired : DailyClaimResult

    data object Refresh : DailyClaimResult

    data object Unavailable : DailyClaimResult
}

sealed interface DailyRestoreResult {
    data class Restored(
        val streak: DailyRestoredStreak,
    ) : DailyRestoreResult

    data object AccountRequired : DailyRestoreResult

    data object Refresh : DailyRestoreResult

    data object Unavailable : DailyRestoreResult
}

enum class DailyMenuFeedbackType {
    CLAIMED,
    RESTORED,
    UNAVAILABLE,
}

data class DailyMenuFeedback(
    val type: DailyMenuFeedbackType,
    val coins: Long? = null,
    val primes: Long? = null,
    val streak: Int? = null,
)

data class DailyMenuScreenData(
    val result: DailyRewardStateResult,
    val feedback: DailyMenuFeedback?,
)
