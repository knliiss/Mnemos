package com.mnemos.reward.domain.daily.repository

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.reward.domain.daily.model.DailyStreak
import java.time.LocalDate

interface DailyStreakRepository {
    fun findByAccountId(accountId: AccountId): DailyStreak?

    fun findByAccountIdForUpdate(accountId: AccountId): DailyStreak?

    fun save(streak: DailyStreak): DailyStreak

    fun findMissedForUpdate(cutoff: LocalDate): List<DailyStreak>
}
