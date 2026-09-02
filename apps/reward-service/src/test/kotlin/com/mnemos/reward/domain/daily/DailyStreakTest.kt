package com.mnemos.reward.domain.daily

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.reward.domain.daily.model.DailyStreak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

class DailyStreakTest {
    private val accountId = AccountId.generate()

    @Test
    fun `claim starts and increments streak`() {
        val day = LocalDate.of(2026, 8, 10)
        val streak = DailyStreak.create(accountId, day)

        assertEquals(1, streak.claim(day))
        assertEquals(2, streak.claim(day.plusDays(1)))
    }

    @Test
    fun `missed day breaks streak and preserves recoverable value`() {
        val day = LocalDate.of(2026, 8, 10)
        val streak =
            DailyStreak(
                accountId = accountId,
                currentStreak = 17,
                lastClaimedDate = day,
                recoverableStreak = 0,
                restoresUsed = 0,
                restoresMonth = YearMonth.of(2026, 8),
            )

        assertTrue(streak.breakIfMissed(day.plusDays(2)))
        assertEquals(0, streak.currentStreak)
        assertEquals(17, streak.recoverableStreak)
    }

    @Test
    fun `restore makes today claimable without immediately breaking again`() {
        val today = LocalDate.of(2026, 8, 12)
        val streak =
            DailyStreak(
                accountId = accountId,
                currentStreak = 0,
                lastClaimedDate = today.minusDays(2),
                recoverableStreak = 10,
                restoresUsed = 0,
                restoresMonth = YearMonth.of(2026, 8),
            )

        assertEquals(10, streak.restore(today, 2))
        assertEquals(today.minusDays(1), streak.lastClaimedDate)
        assertFalse(streak.breakIfMissed(today))
        assertTrue(streak.canClaim(today))
        assertEquals(1, streak.restoresUsed)
    }

    @Test
    fun `second same-day restore cannot consume another restore`() {
        val today = LocalDate.of(2026, 8, 12)
        val streak =
            DailyStreak(
                accountId = accountId,
                currentStreak = 0,
                lastClaimedDate = today.minusDays(2),
                recoverableStreak = 10,
                restoresUsed = 0,
                restoresMonth = YearMonth.of(2026, 8),
            )

        streak.restore(today, 2)
        streak.breakIfMissed(today)

        assertThrows(IllegalArgumentException::class.java) {
            streak.restore(today, 2)
        }
        assertEquals(1, streak.restoresUsed)
        assertEquals(1, streak.restoresRemaining(YearMonth.of(2026, 8), 2))
    }

    @Test
    fun `claim after restore continues recovered streak`() {
        val today = LocalDate.of(2026, 8, 12)
        val streak =
            DailyStreak(
                accountId = accountId,
                currentStreak = 0,
                lastClaimedDate = today.minusDays(2),
                recoverableStreak = 10,
                restoresUsed = 0,
                restoresMonth = YearMonth.of(2026, 8),
            )

        streak.restore(today, 2)

        assertEquals(11, streak.claim(today))
        assertEquals(today, streak.lastClaimedDate)
    }

    @Test
    fun `restore is limited twice per calendar month`() {
        val day = LocalDate.of(2026, 8, 12)
        val streak =
            DailyStreak(
                accountId = accountId,
                currentStreak = 0,
                lastClaimedDate = day.minusDays(2),
                recoverableStreak = 10,
                restoresUsed = 1,
                restoresMonth = YearMonth.of(2026, 8),
            )

        assertEquals(1, streak.restoresRemaining(YearMonth.of(2026, 8), 2))
        assertEquals(10, streak.restore(day, 2))
        assertEquals(0, streak.restoresRemaining(YearMonth.of(2026, 8), 2))
    }

    @Test
    fun `restore counter resets in new month`() {
        val streak =
            DailyStreak(
                accountId = accountId,
                currentStreak = 0,
                lastClaimedDate = LocalDate.of(2026, 8, 30),
                recoverableStreak = 22,
                restoresUsed = 2,
                restoresMonth = YearMonth.of(2026, 8),
            )

        assertEquals(2, streak.restoresRemaining(YearMonth.of(2026, 9), 2))
        assertEquals(22, streak.restore(LocalDate.of(2026, 9, 1), 2))
        assertEquals(1, streak.restoresUsed)
    }
}
