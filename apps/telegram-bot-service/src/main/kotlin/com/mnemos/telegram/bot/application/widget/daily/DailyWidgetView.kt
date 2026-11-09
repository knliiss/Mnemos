package com.mnemos.telegram.bot.application.widget.daily

import java.util.UUID

data class DailyWidgetView(
    val accountId: UUID?,
    val streak: Int?,
    val claimedToday: Boolean?,
    val recoverable: Boolean,
    val recoverableStreak: Int,
    val restoresRemaining: Int,
    val nextCoinMin: Long?,
    val nextCoinMax: Long?,
    val primeChance: Double?,
    val rewardDate: String?,
)
