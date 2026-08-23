package com.mnemos.reward.application.daily.usecase.restore

import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService
import com.mnemos.reward.configuration.RewardProperties
import com.mnemos.reward.domain.daily.exception.DailyStreakNotRecoverableException
import com.mnemos.reward.domain.daily.exception.DailyStreakRestoreLimitReachedException
import com.mnemos.reward.domain.daily.repository.DailyStreakRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

@Service
class RestoreDailyStreakService(
    private val activeAccountProvider: ActiveAccountProvider,
    private val repository: DailyStreakRepository,
    private val properties: RewardProperties,
    private val clock: Clock,
) {
    @Transactional
    fun restore(): Int {
        val accountId = activeAccountProvider.getActiveAccountContext().accountId
        val today = LocalDate.now(clock.withZone(ClaimDailyRewardService.MOSCOW))
        val streak =
            repository.findByAccountIdForUpdate(accountId)
                ?: throw DailyStreakNotRecoverableException()

        streak.breakIfMissed(today)

        if (streak.currentStreak != 0 || streak.recoverableStreak <= 0) {
            throw DailyStreakNotRecoverableException()
        }

        if (streak.restoresRemaining(YearMonth.from(today), properties.daily.monthlyRestoreLimit) <= 0) {
            throw DailyStreakRestoreLimitReachedException()
        }

        val restored = streak.restore(today, properties.daily.monthlyRestoreLimit)
        repository.save(streak)

        return restored
    }
}
