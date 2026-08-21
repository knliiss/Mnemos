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
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class ClaimDailyRewardServiceTest {
    @Test
    fun `claim credits random coins and prime when roll succeeds`() {
        val accountId = AccountId.generate()
        val userId = UserId.generate()
        val repository = MemoryRepo()
        val wallet = RecordingWallet()
        val accountProvider =
            RecordingActiveAccountProvider(
                ActiveAccountContext(
                    accountId = accountId,
                    userId = userId,
                ),
            )
        val clock =
            Clock.fixed(
                Instant.parse("2026-08-11T10:00:00Z"),
                ZoneOffset.UTC,
            )

        val service =
            ClaimDailyRewardService(
                activeAccountProvider = accountProvider,
                repository = repository,
                wallet = wallet,
                random =
                    FixedRandom(
                        long = 60,
                        double = 0.0,
                    ),
                properties = RewardProperties(),
                clock = clock,
            )

        val result = service.claim()

        assertEquals(1, result.streak)
        assertEquals(60, result.coins)
        assertEquals(1, result.primes)
        assertEquals(1, accountProvider.calls)
        assertEquals(
            listOf(
                Currency.COIN to 60L,
                Currency.PRIME to 1L,
            ),
            wallet.credits,
        )
    }

    @Test
    fun `prime chance grows with streak and is capped`() {
        val accountProvider =
            RecordingActiveAccountProvider(
                ActiveAccountContext(
                    accountId = AccountId.generate(),
                    userId = UserId.generate(),
                ),
            )
        val service =
            ClaimDailyRewardService(
                activeAccountProvider = accountProvider,
                repository = MemoryRepo(),
                wallet = RecordingWallet(),
                random =
                    FixedRandom(
                        long = 1,
                        double = 1.0,
                    ),
                properties = RewardProperties(),
                clock = Clock.systemUTC(),
            )

        assertEquals(0.001, service.primeChance(1), 0.0000001)
        assertEquals(0.0039, service.primeChance(30), 0.0000001)
        assertEquals(0.01, service.primeChance(500), 0.0000001)
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
        var value: DailyStreak? = null

        override fun findByAccountId(accountId: AccountId): DailyStreak? = value

        override fun findByAccountIdForUpdate(accountId: AccountId): DailyStreak? = value

        override fun save(streak: DailyStreak): DailyStreak {
            value = streak

            return streak
        }

        override fun findMissedForUpdate(cutoff: LocalDate): List<DailyStreak> = emptyList()
    }

    private class RecordingWallet : RewardWallet {
        val credits = mutableListOf<Pair<Currency, Long>>()

        override fun creditDailyCoin(
            accountId: AccountId,
            amount: Long,
            rewardDate: LocalDate,
        ) {
            credits += Currency.COIN to amount
        }

        override fun creditDailyPrime(
            userId: UserId,
            amount: Long,
            rewardDate: LocalDate,
        ) {
            credits += Currency.PRIME to amount
        }

        override fun creditItemDrop(
            accountId: AccountId,
            amount: Long,
            eventId: UUID,
        ) {
            credits += Currency.COIN to amount
        }
    }

    private class FixedRandom(
        private val long: Long,
        private val double: Double,
    ) : RewardRandom {
        override fun nextLong(
            fromInclusive: Long,
            toInclusive: Long,
        ): Long = long

        override fun nextDouble(): Double = double
    }
}
