package com.mnemos.reward.domain.daily.model

import com.mnemos.common.core.model.id.AccountId
import java.time.LocalDate
import java.time.YearMonth

class DailyStreak(
    val accountId: AccountId,
    currentStreak: Int,
    lastClaimedDate: LocalDate?,
    recoverableStreak: Int,
    restoresUsed: Int,
    restoresMonth: YearMonth,
) {
    var currentStreak: Int = currentStreak
        private set
    var lastClaimedDate: LocalDate? = lastClaimedDate
        private set
    var recoverableStreak: Int = recoverableStreak
        private set
    var restoresUsed: Int = restoresUsed
        private set
    var restoresMonth: YearMonth = restoresMonth
        private set

    fun canClaim(today: LocalDate): Boolean = lastClaimedDate != today

    fun claim(today: LocalDate): Int {
        require(canClaim(today)) { "Daily reward already claimed for $today" }
        currentStreak = if (currentStreak <= 0) 1 else currentStreak + 1
        lastClaimedDate = today
        recoverableStreak = 0
        return currentStreak
    }

    fun breakIfMissed(today: LocalDate): Boolean {
        val lastClaim = lastClaimedDate ?: return false
        if (currentStreak <= 0 || !lastClaim.isBefore(today.minusDays(1))) return false
        recoverableStreak = currentStreak
        currentStreak = 0
        return true
    }

    fun restoresRemaining(
        month: YearMonth,
        limit: Int,
    ): Int = if (restoresMonth == month) (limit - restoresUsed).coerceAtLeast(0) else limit

    fun restore(
        today: LocalDate,
        limit: Int,
    ): Int {
        val month = YearMonth.from(today)
        normalizeRestoreMonth(month)
        require(currentStreak == 0 && recoverableStreak > 0) { "Daily streak is not recoverable" }
        require(restoresUsed < limit) { "Monthly streak restore limit reached" }
        currentStreak = recoverableStreak
        recoverableStreak = 0
        restoresUsed++
        return currentStreak
    }

    private fun normalizeRestoreMonth(month: YearMonth) {
        if (restoresMonth != month) {
            restoresMonth = month
            restoresUsed = 0
        }
    }

    companion object {
        fun create(
            accountId: AccountId,
            today: LocalDate,
        ): DailyStreak = DailyStreak(accountId, 0, null, 0, 0, YearMonth.from(today))
    }
}
