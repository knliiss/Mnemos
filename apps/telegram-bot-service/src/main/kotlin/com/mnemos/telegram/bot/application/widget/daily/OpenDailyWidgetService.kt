package com.mnemos.telegram.bot.application.widget.daily

import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.daily.port.out.DailyWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenDailyWidgetService(
    private val properties: TelegramWidgetProperties,
    private val rewards: DailyRewardGateway,
    private val publisher: DailyWidgetPublisher,
) : OpenDailyWidgetUseCase,
    DailyWidgetSnapshotQuery {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(telegramId: Long): TelegramDeliveryResult {
        if (!enabled) return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        return when (val snapshot = load(telegramId)) {
            is DailyWidgetSnapshotResult.Available -> publisher.publish(telegramId, snapshot.view)
            DailyWidgetSnapshotResult.Unavailable -> TelegramDeliveryResult.Retryable
        }
    }

    override fun load(telegramId: Long): DailyWidgetSnapshotResult {
        val reward =
            when (val result = rewards.getState(telegramId)) {
                is DailyRewardStateResult.Available -> result.state
                DailyRewardStateResult.AccountRequired -> null
                DailyRewardStateResult.Unavailable -> return DailyWidgetSnapshotResult.Unavailable
            }

        return DailyWidgetSnapshotResult.Available(
            DailyWidgetView(
                accountId = reward?.accountId,
                streak = reward?.currentStreak,
                claimedToday = reward?.claimedToday,
                recoverable = reward?.recoverable ?: false,
                recoverableStreak = reward?.recoverableStreak ?: 0,
                restoresRemaining = reward?.restoresRemaining ?: 0,
                nextCoinMin = reward?.nextCoinMin,
                nextCoinMax = reward?.nextCoinMax,
                primeChance = reward?.primeChance,
                rewardDate = reward?.rewardDate?.toString(),
            ),
        )
    }
}
