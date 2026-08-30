package com.mnemos.telegram.bot.adapter.out.reward

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyRestoreResult
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardClaimResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardRestoreResponse
import com.mnemos.telegram.bot.contracts.internal.daily.TelegramDailyRewardStateResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class DailyRewardGatewayAdapterTest {
    @Test
    fun `gateway maps available daily state`() {
        val accountId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val client =
            FakeRewardServiceClient(
                state =
                    TelegramDailyRewardStateResponse(
                        accountId = accountId,
                        currentStreak = 7,
                        claimedToday = false,
                        recoverable = true,
                        recoverableStreak = 12,
                        restoresRemaining = 1,
                        nextCoinMin = 70,
                        nextCoinMax = 90,
                        primeChance = 0.002,
                        rewardDate = LocalDate.of(2026, 8, 30),
                    ),
            )
        val gateway = DailyRewardGatewayAdapter(client)

        val result = gateway.getState(931458258) as DailyRewardStateResult.Available

        assertEquals(accountId, result.state.accountId)
        assertEquals(7, result.state.currentStreak)
        assertEquals(12, result.state.recoverableStreak)
        assertEquals(70, result.state.nextCoinMin)
        assertEquals(90, result.state.nextCoinMax)
    }

    @Test
    fun `gateway maps successful claim and restore`() {
        val client =
            FakeRewardServiceClient(
                claim =
                    TelegramDailyRewardClaimResponse(
                        streak = 8,
                        coins = 81,
                        primes = 1,
                        primeChance = 0.002,
                    ),
                restore = TelegramDailyRewardRestoreResponse(currentStreak = 12),
            )
        val gateway = DailyRewardGatewayAdapter(client)

        val claimResult = gateway.claim(931458258) as DailyClaimResult.Claimed
        val restoreResult = gateway.restore(931458258) as DailyRestoreResult.Restored

        assertEquals(8, claimResult.reward.streak)
        assertEquals(81, claimResult.reward.coins)
        assertEquals(1, claimResult.reward.primes)
        assertEquals(12, restoreResult.streak.currentStreak)
    }

    @Test
    fun `gateway treats missing Telegram account context as account required`() {
        val gateway = DailyRewardGatewayAdapter(FakeRewardServiceClient())

        assertSame(DailyRewardStateResult.AccountRequired, gateway.getState(931458258))
        assertSame(DailyClaimResult.AccountRequired, gateway.claim(931458258))
        assertSame(DailyRestoreResult.AccountRequired, gateway.restore(931458258))
    }

    @Test
    fun `gateway reports unavailable service failures`() {
        val gateway = DailyRewardGatewayAdapter(FailingRewardServiceClient())

        assertSame(DailyRewardStateResult.Unavailable, gateway.getState(931458258))
        assertSame(DailyClaimResult.Unavailable, gateway.claim(931458258))
        assertSame(DailyRestoreResult.Unavailable, gateway.restore(931458258))
    }

    private class FakeRewardServiceClient(
        private val state: TelegramDailyRewardStateResponse? = null,
        private val claim: TelegramDailyRewardClaimResponse? = null,
        private val restore: TelegramDailyRewardRestoreResponse? = null,
    ) : RewardServiceClient {
        override fun getDailyState(telegramId: Long): TelegramDailyRewardStateResponse? = state

        override fun claimDaily(telegramId: Long): TelegramDailyRewardClaimResponse? = claim

        override fun restoreDaily(telegramId: Long): TelegramDailyRewardRestoreResponse? = restore
    }

    private class FailingRewardServiceClient : RewardServiceClient {
        override fun getDailyState(telegramId: Long): TelegramDailyRewardStateResponse = error("reward unavailable")

        override fun claimDaily(telegramId: Long): TelegramDailyRewardClaimResponse = error("reward unavailable")

        override fun restoreDaily(telegramId: Long): TelegramDailyRewardRestoreResponse = error("reward unavailable")
    }
}
