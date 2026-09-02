package com.mnemos.telegram.bot.application.daily

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyRestoreResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class DailyCallbackHandler(
    private val gateway: DailyRewardGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = DailyCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (!isCurrentDailyMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        return when (context.data) {
            DailyCallbackData.REFRESH -> update(context, DailyMenuContext.clear())
            DailyCallbackData.CLAIM -> claim(context)
            DailyCallbackData.RESTORE -> restore(context)
            else -> TelegramDeliveryResult.Delivered
        }
    }

    private fun claim(context: TelegramCallbackContext): TelegramDeliveryResult =
        when (val result = gateway.claim(context.telegramId)) {
            is DailyClaimResult.Claimed -> update(context, DailyMenuContext.claimed(result.reward))
            DailyClaimResult.AccountRequired,
            DailyClaimResult.Refresh,
            -> update(context, DailyMenuContext.clear())

            DailyClaimResult.Unavailable -> update(context, DailyMenuContext.unavailable())
        }

    private fun restore(context: TelegramCallbackContext): TelegramDeliveryResult =
        when (val result = gateway.restore(context.telegramId)) {
            is DailyRestoreResult.Restored -> update(context, DailyMenuContext.restored(result.streak))
            DailyRestoreResult.AccountRequired,
            DailyRestoreResult.Refresh,
            -> update(context, DailyMenuContext.clear())

            DailyRestoreResult.Unavailable -> update(context, DailyMenuContext.unavailable())
        }

    private fun update(
        context: TelegramCallbackContext,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.DAILY,
            update = { screenContext },
        )

    private fun isCurrentDailyMenu(context: TelegramCallbackContext): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.DAILY && session.menuMessageId == context.messageId
    }
}
