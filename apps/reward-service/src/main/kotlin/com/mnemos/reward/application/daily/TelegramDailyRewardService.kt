package com.mnemos.reward.application.daily

import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.reward.application.daily.model.ClaimedDailyReward
import com.mnemos.reward.application.daily.model.DailyRewardState
import com.mnemos.reward.application.daily.port.out.TelegramActiveAccountProvider
import com.mnemos.reward.application.daily.usecase.claim.ClaimDailyRewardService
import com.mnemos.reward.application.daily.usecase.get.GetDailyRewardStateService
import com.mnemos.reward.application.daily.usecase.restore.RestoreDailyStreakService
import org.springframework.stereotype.Service

@Service
class TelegramDailyRewardService(
    private val accountProvider: TelegramActiveAccountProvider,
    private val getStateService: GetDailyRewardStateService,
    private val claimService: ClaimDailyRewardService,
    private val restoreService: RestoreDailyStreakService,
) {
    fun getState(telegramId: TelegramId): DailyRewardState? {
        val activeAccount = accountProvider.findByTelegramId(telegramId) ?: return null

        return getStateService.get(activeAccount)
    }

    fun claim(telegramId: TelegramId): ClaimedDailyReward? {
        val activeAccount = accountProvider.findByTelegramId(telegramId) ?: return null

        return claimService.claim(activeAccount)
    }

    fun restore(telegramId: TelegramId): Int? {
        val activeAccount = accountProvider.findByTelegramId(telegramId) ?: return null

        return restoreService.restore(activeAccount)
    }
}
