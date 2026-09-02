package com.mnemos.reward.application.daily

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.reward.application.daily.port.out.ActiveAccountContext
import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.application.daily.port.out.RewardRandom
import com.mnemos.reward.application.daily.port.out.RewardWallet
import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService
import com.mnemos.reward.configuration.RewardProperties
import com.mnemos.reward.domain.daily.model.DailyStreak
import com.mnemos.reward.domain.daily.repository.DailyStreakRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class ClaimDailyRewardExplicitContextTest {
    @Test
    fun `explicit account context bypasses current user provider`() {
        val fallbackContext =
            ActiveAccountContext(
                accountId = AccountId.generate(),
                userId = UserId.generate(),
            )
        val explicitContext =
            ActiveAccountContext(
                accountId = AccountId.generate(),
                userId = UserId.generate(),
            )
        val activeAccountProvider = RecordingActiveAccountProvider(fallbackContext)
        val wallet = RecordingWallet()
        val service =
            ClaimDailyRewardService(
                activeAccountProvider = activeAccountProvider,
                repository = MemoryRepo(),
                wallet = wallet,
                random = FixedRandom(),
                properties = RewardProperties(),
                clock =
                    Clock.fixed(
                        Instant.parse("2026-08-30T12:00:00Z"),
                        ZoneOffset.UTC,
                    ),
            )

        val result = service.claim(explicitContext)

        assertEquals(0, activeAccountProvider.calls)
        assertEquals(1, result.streak)
        assertEquals(explicitContext.accountId, wallet.coinAccountId)
        assertEquals(explicitContext.userId, wallet.primeUserId)
    }

    private class RecordingActiveAccountProvider(
        private val context: ActiveAccountContext,
    ) : ActiveAccountProvider {
        var calls = 0

        override fun getActiveAccountContext(): ActiveAccountContext {
            calls += 1

            return context
        }
    }

    private class MemoryRepo : DailyStreakRepository {
        private var streak: DailyStreak? = null

        override fun findByAccountId(accountId: AccountId): DailyStreak? = streak

        override fun findByAccountIdForUpdate(accountId: AccountId): DailyStreak? = streak

        override fun save(streak: DailyStreak): DailyStreak {
            this.streak = streak

            return streak
        }

        override fun findMissedForUpdate(cutoff: LocalDate): List<DailyStreak> = emptyList()
    }

    private class RecordingWallet : RewardWallet {
        var coinAccountId: AccountId? = null
        var primeUserId: UserId? = null

        override fun creditDailyCoin(
            accountId: AccountId,
            amount: Long,
            rewardDate: LocalDate,
        ) {
            coinAccountId = accountId
        }

        override fun creditDailyPrime(
            userId: UserId,
            amount: Long,
            rewardDate: LocalDate,
        ) {
            primeUserId = userId
        }

        override fun creditItemDrop(
            accountId: AccountId,
            amount: Long,
            eventId: UUID,
        ) = Unit
    }

    private class FixedRandom : RewardRandom {
        override fun nextLong(
            fromInclusive: Long,
            toInclusive: Long,
        ): Long = fromInclusive

        override fun nextDouble(): Double = 0.0
    }
}
