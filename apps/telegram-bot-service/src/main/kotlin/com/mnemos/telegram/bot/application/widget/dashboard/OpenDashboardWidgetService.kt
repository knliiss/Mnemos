package com.mnemos.telegram.bot.application.widget.dashboard

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OpenDashboardWidgetService(
    private val properties: TelegramWidgetProperties,
    private val accounts: AccountGateway,
    private val balances: AccountBalanceGateway,
    private val rewards: DailyRewardGateway,
    private val publisher: DashboardWidgetPublisher,
) : OpenDashboardWidgetUseCase {
    override val enabled: Boolean
        get() = properties.enabled

    override fun execute(telegramId: Long): TelegramDeliveryResult {
        if (!enabled) {
            return TelegramDeliveryResult.PermanentFailure("widgets-disabled")
        }

        val accountResult = accounts.getContext(telegramId)
        if (accountResult == TelegramAccountContextResult.Unavailable) {
            return TelegramDeliveryResult.Retryable
        }

        val context = (accountResult as? TelegramAccountContextResult.Available)?.context
        val activeAccount = context?.accounts?.firstOrNull { account ->
            account.accountId == context.activeAccountId
        }
        val balance =
            if (activeAccount == null) {
                null
            } else {
                (balances.getBalances(telegramId) as? TelegramAccountBalanceResult.Available)?.balance
            }
        val reward =
            if (activeAccount == null) {
                null
            } else {
                (rewards.getState(telegramId) as? DailyRewardStateResult.Available)?.state
            }

        val view =
            DashboardWidgetView(
                accountName = activeAccount?.cristalixNickname,
                accountCount = context?.accounts?.size ?: 0,
                coin = balance?.coin,
                prime = balance?.prime,
                dailyStreak = reward?.currentStreak,
                dailyClaimed = reward?.claimedToday,
            )

        return publisher.publish(telegramId, view)
    }
}
