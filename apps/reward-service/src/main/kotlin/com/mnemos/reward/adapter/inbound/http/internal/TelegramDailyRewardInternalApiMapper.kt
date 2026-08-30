package com.mnemos.reward.adapter.inbound.http.internal

import com.mnemos.reward.application.daily.model.ClaimedDailyReward
import com.mnemos.reward.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardClaimResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardRestoreResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardStateResponse

object TelegramDailyRewardInternalApiMapper {
    fun DailyRewardState.toResponse(): TelegramDailyRewardStateResponse =
        TelegramDailyRewardStateResponse(
            accountId = accountId.id,
            currentStreak = currentStreak,
            claimedToday = claimedToday,
            recoverable = recoverable,
            recoverableStreak = recoverableStreak,
            restoresRemaining = restoresRemaining,
            nextCoinMin = nextCoinMin,
            nextCoinMax = nextCoinMax,
            primeChance = primeChance,
            rewardDate = rewardDate,
        )

    fun ClaimedDailyReward.toResponse(): TelegramDailyRewardClaimResponse =
        TelegramDailyRewardClaimResponse(
            streak = streak,
            coins = coins,
            primes = primes,
            primeChance = primeChance,
        )

    fun Int.toRestoreResponse(): TelegramDailyRewardRestoreResponse =
        TelegramDailyRewardRestoreResponse(currentStreak = this)
}
