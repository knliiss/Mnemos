package com.mnemos.reward.application.daily.usecase.get

import com.mnemos.reward.application.daily.model.DailyRewardState
import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService
import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService.Companion.MOSCOW
import com.mnemos.reward.configuration.RewardProperties
import com.mnemos.reward.domain.daily.repository.DailyStreakRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

@Service
class GetDailyRewardStateService(
    private val activeAccountProvider: ActiveAccountProvider,
    private val repository: DailyStreakRepository,
    private val claimService: ClaimDailyRewardService,
    private val properties: RewardProperties,
    private val clock: Clock,
) {
    fun get(): DailyRewardState {
        val accountId = activeAccountProvider.getActiveAccountId()
        val today = LocalDate.now(clock.withZone(MOSCOW))

        val streak = repository.findByAccountId(accountId)

        val missed =
            streak?.let {
                it.currentStreak > 0 &&
                    it.lastClaimedDate?.isBefore(today.minusDays(1)) == true
            } ?: false

        val currentStreak =
            if (missed) {
                0
            } else {
                streak?.currentStreak ?: 0
            }

        val recoverableStreak =
            if (missed) {
                streak.currentStreak
            } else {
                streak?.recoverableStreak ?: 0
            }

        val nextStreak =
            if (currentStreak <= 0) {
                1
            } else {
                currentStreak + 1
            }

        val rewardRange =
            properties.daily.coinRanges[
                (nextStreak - 1) % properties.daily.coinRanges.size,
            ]
        return DailyRewardState(
            accountId = accountId,
            currentStreak = currentStreak,
            claimedToday = streak?.lastClaimedDate == today,
            recoverable = currentStreak == 0 && recoverableStreak > 0,
            recoverableStreak = recoverableStreak,
            restoresRemaining =
                streak?.restoresRemaining(YearMonth.from(today), properties.daily.monthlyRestoreLimit)
                    ?: properties.daily.monthlyRestoreLimit,
            nextCoinMin = rewardRange.min,
            nextCoinMax = rewardRange.max,
            primeChance = claimService.primeChance(nextStreak),
            rewardDate = today,
        )
    }
}
