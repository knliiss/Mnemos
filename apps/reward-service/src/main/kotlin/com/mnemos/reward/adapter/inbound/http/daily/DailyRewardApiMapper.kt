package com.mnemos.reward.adapter.inbound.http.daily

import com.mnemos.reward.application.daily.model.ClaimedDailyReward
import com.mnemos.reward.application.daily.model.DailyRewardState

object DailyRewardApiMapper {
    fun DailyRewardState.toResponse(): DailyRewardStateResponse =
        DailyRewardStateResponse(
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

    fun ClaimedDailyReward.toResponse(): ClaimDailyRewardResponse =
        ClaimDailyRewardResponse(
            streak = streak,
            coins = coins,
            primes = primes,
            primeChance = primeChance,
        )
}
