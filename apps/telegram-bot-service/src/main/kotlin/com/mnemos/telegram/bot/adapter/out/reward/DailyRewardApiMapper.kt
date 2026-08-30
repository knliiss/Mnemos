package com.mnemos.telegram.bot.adapter.out.reward

import com.mnemos.telegram.bot.application.daily.model.DailyClaimedReward
import com.mnemos.telegram.bot.application.daily.model.DailyRestoredStreak
import com.mnemos.telegram.bot.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardClaimResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardRestoreResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardStateResponse

object DailyRewardApiMapper {
    fun TelegramDailyRewardStateResponse.toModel(): DailyRewardState =
        DailyRewardState(
            accountId = accountId,
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

    fun TelegramDailyRewardClaimResponse.toModel(): DailyClaimedReward =
        DailyClaimedReward(
            streak = streak,
            coins = coins,
            primes = primes,
        )

    fun TelegramDailyRewardRestoreResponse.toModel(): DailyRestoredStreak =
        DailyRestoredStreak(currentStreak = currentStreak)
}
