package com.mnemos.telegram.bot.contracts.internal.daily

import java.time.LocalDate
import java.util.UUID

data class TelegramDailyRewardStateResponse(
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

data class TelegramDailyRewardClaimResponse(
    val streak: Int,
    val coins: Long,
    val primes: Long,
    val primeChance: Double,
)

data class TelegramDailyRewardRestoreResponse(
    val currentStreak: Int,
)
