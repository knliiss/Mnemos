package com.mnemos.telegram.bot.application.widget.dashboard

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
import com.mnemos.telegram.bot.application.widget.notifications.OpenNotificationWidgetUseCase
import com.mnemos.telegram.bot.application.widget.events.OpenEventWidgetUseCase
import com.mnemos.telegram.bot.application.widget.accounts.OpenAccountWidgetUseCase
import com.mnemos.telegram.bot.application.widget.daily.OpenDailyWidgetUseCase
import com.mnemos.telegram.bot.application.widget.bets.OpenBetWidgetUseCase
import com.mnemos.telegram.bot.application.widget.wallet.OpenWalletWidgetUseCase
import com.mnemos.telegram.bot.application.widget.online.OpenOnlineWidgetUseCase
import com.mnemos.telegram.bot.application.widget.settings.OpenSettingsWidgetUseCase
import com.mnemos.telegram.bot.application.widget.profiles.OpenProfileWidgetUseCase
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class DashboardWidgetActionHandler(
    private val snapshots: DashboardWidgetSnapshotQuery,
    private val publisher: DashboardWidgetPublisher,
    private val rewards: DailyRewardGateway,
    private val notifications: OpenNotificationWidgetUseCase,
    private val events: OpenEventWidgetUseCase,
    private val accounts: OpenAccountWidgetUseCase,
    private val daily: OpenDailyWidgetUseCase,
    private val bets: OpenBetWidgetUseCase,
    private val wallet: OpenWalletWidgetUseCase,
    private val online: OpenOnlineWidgetUseCase,
    private val responder: WidgetQueryResponder,
    private val settings: OpenSettingsWidgetUseCase? = null,
    private val profiles: OpenProfileWidgetUseCase? = null,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.DASHBOARD

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val telegramId = context.actorId
        val loaded = snapshots.load(telegramId)
        if (loaded !is DashboardWidgetSnapshotResult.Available) {
            return responder.reply(context.queryId, "UNAVAILABLE", "Немає доступу до даних")
        }

        if (loaded.view.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /widget")
        }

        if (context.data == "w1:prof:open-screen") {
            val delivery = profiles?.takeIf { it.enabled }?.execute(telegramId)
                ?: TelegramDeliveryResult.PermanentFailure("widgets-disabled")
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Пошук профілів відкрито" else "Профілі недоступні",
            )
        }

        if (context.data == "w1:settings:open") {
            val delivery = settings?.takeIf { it.enabled }?.execute(telegramId)
                ?: TelegramDeliveryResult.PermanentFailure("widgets-disabled")
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Налаштування відкрито" else "Не вдалося відкрити налаштування",
            )
        }

        if (context.data == "w1:on:open") {
            val delivery = online.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "SAO Online відкрито" else "SAO Online недоступний",
            )
        }

        if (context.data == "w1:wallet:open") {
            val delivery = wallet.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Гаманець відкрито" else "Не вдалося відкрити гаманець",
            )
        }

        if (context.data == "w1:bet:open") {
            val delivery = bets.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Ставки відкрито" else "Не вдалося відкрити ставки",
            )
        }

        if (context.data == "w1:daily:open") {
            val delivery = daily.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Нагороди відкрито" else "Не вдалося відкрити нагороди",
            )
        }

        if (context.data == "w1:acct:open") {
            val delivery = accounts.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Керування акаунтами відкрито" else "Не вдалося відкрити акаунти",
            )
        }

        if (context.data == "w1:evt:open") {
            val delivery = events.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Стрічку подій відкрито" else "Не вдалося відкрити події",
            )
        }

        if (context.data == "w1:sub:open") {
            val delivery = notifications.execute(telegramId)
            return responder.reply(
                context.queryId,
                if (delivery == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (delivery == TelegramDeliveryResult.Delivered) "Підписки відкрито" else "Не вдалося відкрити підписки",
            )
        }

        val message =
            when (context.data) {
                "w1:refresh" -> "Дані оновлено"
                "w1:claim" -> {
                    if (session.accountId == null) {
                        return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спершу виберіть акаунт")
                    }
                    when (rewards.claim(telegramId)) {
                        is DailyClaimResult.Claimed -> "Нагороду отримано"
                        DailyClaimResult.Refresh -> "Нагорода вже отримана або недоступна"
                        DailyClaimResult.AccountRequired -> "Спершу виберіть акаунт"
                        DailyClaimResult.Unavailable ->
                            return responder.reply(context.queryId, "UNAVAILABLE", "Спробуйте пізніше")
                    }
                }

                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        val refreshed =
            if (context.data == "w1:refresh") {
                loaded.view
            } else {
                (snapshots.load(telegramId) as? DashboardWidgetSnapshotResult.Available)?.view
            }

        if (refreshed == null) {
            return responder.reply(context.queryId, "OK", message)
        }

        val response =
            responder.reply(
                context.queryId,
                "OK",
                message,
                state = publisher.state(refreshed),
            )

        if (response == TelegramDeliveryResult.Delivered) {
            publisher.update(telegramId, context.messageId, refreshed)
        }
        return response
    }
}
