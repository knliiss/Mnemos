package com.mnemos.reward.application.daily.usecase.reset

import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService
import com.mnemos.reward.domain.daily.repository.DailyStreakRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate

@Service
class ResetMissedDailyStreaksService(
    private val repository: DailyStreakRepository,
    private val clock: Clock,
) {
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    @Transactional
    fun resetMissed() {
        val today = LocalDate.now(clock.withZone(ClaimDailyRewardService.MOSCOW))
        repository.findMissedForUpdate(today.minusDays(1)).forEach { streak ->
            if (streak.breakIfMissed(today)) repository.save(streak)
        }
    }
}
