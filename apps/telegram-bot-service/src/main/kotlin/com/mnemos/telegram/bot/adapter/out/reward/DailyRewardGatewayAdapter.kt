package com.mnemos.telegram.bot.adapter.out.reward

import com.mnemos.telegram.bot.adapter.out.reward.DailyRewardApiMapper.toModel
import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyRestoreResult
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import feign.FeignException
import org.springframework.stereotype.Component

@Component
class DailyRewardGatewayAdapter(
    private val client: RewardServiceClient,
) : DailyRewardGateway {
    override fun getState(telegramId: Long): DailyRewardStateResult =
        try {
            client
                .getDailyState(telegramId)
                ?.let { response -> DailyRewardStateResult.Available(response.toModel()) }
                ?: DailyRewardStateResult.AccountRequired
        } catch (_: RuntimeException) {
            DailyRewardStateResult.Unavailable
        }

    override fun claim(telegramId: Long): DailyClaimResult =
        try {
            client
                .claimDaily(telegramId)
                ?.let { response -> DailyClaimResult.Claimed(response.toModel()) }
                ?: DailyClaimResult.AccountRequired
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                DailyClaimResult.Refresh
            } else {
                DailyClaimResult.Unavailable
            }
        } catch (_: RuntimeException) {
            DailyClaimResult.Unavailable
        }

    override fun restore(telegramId: Long): DailyRestoreResult =
        try {
            client
                .restoreDaily(telegramId)
                ?.let { response -> DailyRestoreResult.Restored(response.toModel()) }
                ?: DailyRestoreResult.AccountRequired
        } catch (exception: FeignException) {
            if (exception.status() in 400..499) {
                DailyRestoreResult.Refresh
            } else {
                DailyRestoreResult.Unavailable
            }
        } catch (_: RuntimeException) {
            DailyRestoreResult.Unavailable
        }
}
