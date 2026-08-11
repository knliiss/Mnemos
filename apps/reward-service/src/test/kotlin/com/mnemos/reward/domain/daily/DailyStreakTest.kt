package com.mnemos.reward.domain.daily

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.reward.domain.daily.model.DailyStreak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class DailyStreakTest {
    private val accountId = AccountId.generate()

    @Test fun `claim starts and increments streak`() {
        val day = LocalDate.of(2026, 8, 10)
        val streak = DailyStreak.create(accountId, day)
        assertEquals(1, streak.claim(day))
        assertEquals(2, streak.claim(day.plusDays(1)))
    }

    @Test fun `missed day breaks streak and preserves recoverable value`() {
        val day = LocalDate.of(2026, 8, 10)
        val streak = DailyStreak(accountId, 17, day, 0, 0, YearMonth.of(2026, 8))
        assertTrue(streak.breakIfMissed(day.plusDays(2)))
        assertEquals(0, streak.currentStreak)
        assertEquals(17, streak.recoverableStreak)
    }

    @Test fun `restore is limited twice per calendar month`() {
        val day = LocalDate.of(2026, 8, 12)
        val streak = DailyStreak(accountId, 0, day.minusDays(2), 10, 1, YearMonth.of(2026, 8))
        assertEquals(1, streak.restoresRemaining(YearMonth.of(2026, 8), 2))
        assertEquals(10, streak.restore(day, 2))
        assertEquals(0, streak.restoresRemaining(YearMonth.of(2026, 8), 2))
    }

    @Test fun `restore counter resets in new month`() {
        val streak = DailyStreak(accountId, 0, LocalDate.of(2026, 8, 30), 22, 2, YearMonth.of(2026, 8))
        assertEquals(2, streak.restoresRemaining(YearMonth.of(2026, 9), 2))
        assertEquals(22, streak.restore(LocalDate.of(2026, 9, 1), 2))
        assertEquals(1, streak.restoresUsed)
    }
}
