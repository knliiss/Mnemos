package com.mnemos.reward.application.daily.model

import com.mnemos.common.core.model.id.AccountId
import java.time.LocalDate

data class DailyRewardState(
    val accountId: AccountId,
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
