package com.mnemos.reward.application.daily.usecase.claim

import com.mnemos.reward.application.daily.model.ClaimedDailyReward
import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.application.daily.port.out.RewardRandom
import com.mnemos.reward.application.daily.port.out.RewardWallet
import com.mnemos.reward.configuration.RewardProperties
import com.mnemos.reward.domain.daily.exception.DailyRewardAlreadyClaimedException
import com.mnemos.reward.domain.daily.model.DailyStreak
import com.mnemos.reward.domain.daily.repository.DailyStreakRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

@Service
class ClaimDailyRewardService(
    private val activeAccountProvider: ActiveAccountProvider,
    private val repository: DailyStreakRepository,
    private val wallet: RewardWallet,
    private val random: RewardRandom,
    private val properties: RewardProperties,
    private val clock: Clock,
) {
    @Transactional
    fun claim(): ClaimedDailyReward {
        val activeAccount = activeAccountProvider.getActiveAccountContext()
        val accountId = activeAccount.accountId
        val today = LocalDate.now(clock.withZone(MOSCOW))
        val streak =
            repository.findByAccountIdForUpdate(accountId)
                ?: DailyStreak.create(accountId, today)

        if (!streak.canClaim(today)) {
            throw DailyRewardAlreadyClaimedException()
        }

        streak.breakIfMissed(today)

        val current = streak.claim(today)
        val range = properties.daily.coinRanges[(current - 1) % properties.daily.coinRanges.size]
        val coins = random.nextLong(range.min, range.max)
        val primeChance = primeChance(current)
        val primes =
            if (random.nextDouble() < primeChance) {
                properties.daily.primeAmount
            } else {
                0
            }

        repository.save(streak)
        wallet.creditDailyCoin(accountId, coins, today)

        if (primes > 0) {
            wallet.creditDailyPrime(
                userId = activeAccount.userId,
                amount = primes,
                rewardDate = today,
            )
        }

        return ClaimedDailyReward(current, coins, primes, primeChance)
    }

    fun primeChance(streak: Int): Double =
        (properties.daily.primeBaseChance + (streak - 1).coerceAtLeast(0) * properties.daily.primeChancePerStreak)
            .coerceAtMost(properties.daily.primeMaxChance)

    companion object {
        val MOSCOW: ZoneId = ZoneId.of("Europe/Moscow")
    }
}
