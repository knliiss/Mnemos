package com.mnemos.reward.adapter.inbound.http.daily

import java.time.LocalDate
import java.util.UUID

data class DailyRewardStateResponse(
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
