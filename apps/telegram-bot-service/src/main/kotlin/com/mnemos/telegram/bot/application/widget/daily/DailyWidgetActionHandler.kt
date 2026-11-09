package com.mnemos.telegram.bot.application.widget.daily

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyRestoreResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.daily.port.out.DailyWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class DailyWidgetActionHandler(
    private val snapshots: DailyWidgetSnapshotQuery,
    private val rewards: DailyRewardGateway,
    private val publisher: DailyWidgetPublisher,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.DAILY

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val telegramId = context.actorId
        val loaded = snapshots.load(telegramId)
        if (loaded !is DailyWidgetSnapshotResult.Available) {
            return responder.reply(context.queryId, "UNAVAILABLE", "Нагороди зараз недоступні")
        }
        val current = loaded.view

        if (current.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /daily")
        }

        val message =
            when (context.data) {
                "w1:daily:refresh" -> "Нагороди оновлено"
                "w1:daily:claim" -> {
                    if (current.accountId == null) {
                        return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спершу виберіть акаунт")
                    }
                    if (current.claimedToday == true) {
                        "Нагороду вже отримано сьогодні"
                    } else {
                        when (val result = rewards.claim(telegramId)) {
                            is DailyClaimResult.Claimed ->
                                "Отримано " + result.reward.coins + " COIN і " + result.reward.primes + " PRIME"
                            DailyClaimResult.Refresh -> "Стан нагороди змінився"
                            DailyClaimResult.AccountRequired ->
                                return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спершу виберіть акаунт")
                            DailyClaimResult.Unavailable ->
                                return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося отримати нагороду")
                        }
                    }
                }

                "w1:daily:restore" -> {
                    if (current.accountId == null) {
                        return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спершу виберіть акаунт")
                    }
                    if (!current.recoverable || current.restoresRemaining <= 0) {
                        "Відновлення зараз недоступне"
                    } else {
                        when (val result = rewards.restore(telegramId)) {
                            is DailyRestoreResult.Restored -> "Серію відновлено: " + result.streak.currentStreak
                            DailyRestoreResult.Refresh -> "Стан серії змінився"
                            DailyRestoreResult.AccountRequired ->
                                return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спершу виберіть акаунт")
                            DailyRestoreResult.Unavailable ->
                                return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося відновити серію")
                        }
                    }
                }

                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        val refreshed = (snapshots.load(telegramId) as? DailyWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "OK", message + ". Оновіть картку")

        if (refreshed.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /daily")
        }

        val response =
            responder.reply(
                queryId = context.queryId,
                status = "OK",
                message = message,
                state = publisher.state(refreshed),
            )
        if (response == TelegramDeliveryResult.Delivered) {
            publisher.update(telegramId, context.messageId, refreshed)
        }
        return response
    }
}
