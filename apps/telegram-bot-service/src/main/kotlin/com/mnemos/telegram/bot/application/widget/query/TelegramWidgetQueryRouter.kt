package com.mnemos.telegram.bot.application.widget.query

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.identity.TelegramPrivateActorPolicy
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramWidgetQueryRouter(
    private val sessions: TelegramWidgetSessionRepository,
    private val snapshots: DashboardWidgetSnapshotQuery,
    private val publisher: DashboardWidgetPublisher,
    private val rewards: DailyRewardGateway,
    private val gateway: TelegramWidgetGateway,
    private val json: ObjectMapper,
) {
    fun route(query: TelegramWidgetQueryContext): TelegramDeliveryResult {
        val telegramId =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = query.isPrivate,
                chatId = query.chatId,
                actorId = query.actorId,
            )

        if (telegramId == null || query.isShared) {
            return reply(query.queryId, "FORBIDDEN", "Недоступна дія")
        }

        if (query.type != "data" || query.data.toByteArray(Charsets.UTF_8).size !in 1..64) {
            return reply(query.queryId, "UNSUPPORTED", "Непідтримуваний запит")
        }

        val session = sessions.find(telegramId, query.messageId)
        if (session == null || session.screen != TelegramWidgetScreen.DASHBOARD) {
            return reply(query.queryId, "EXPIRED", "Відкрийте нову картку /widget")
        }

        val loaded = snapshots.load(telegramId)
        if (loaded !is DashboardWidgetSnapshotResult.Available) {
            return reply(query.queryId, "UNAVAILABLE", "Немає доступу до даних")
        }

        val view = loaded.view
        if (view.accountId != session.accountId) {
            return reply(query.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /widget")
        }

        val message =
            when (query.data) {
                "w1:refresh" -> "Дані оновлено"
                "w1:claim" -> {
                    if (session.accountId == null) {
                        return reply(query.queryId, "ACCOUNT_REQUIRED", "Спершу виберіть акаунт")
                    }
                    when (rewards.claim(telegramId)) {
                        is DailyClaimResult.Claimed -> "Нагороду отримано"
                        DailyClaimResult.Refresh -> "Нагорода вже отримана або недоступна"
                        DailyClaimResult.AccountRequired -> "Спершу виберіть акаунт"
                        DailyClaimResult.Unavailable -> return reply(query.queryId, "UNAVAILABLE", "Спробуйте пізніше")
                    }
                }

                else -> return reply(query.queryId, "UNSUPPORTED", "Невідома дія")
            }

        val refreshed =
            if (query.data == "w1:refresh") {
                view
            } else {
                (snapshots.load(telegramId) as? DashboardWidgetSnapshotResult.Available)?.view
            }

        return if (refreshed == null) {
            reply(query.queryId, "OK", message)
        } else {
            val status = reply(query.queryId, "OK", message, refreshed)
            if (status == TelegramDeliveryResult.Delivered) {
                publisher.update(telegramId, query.messageId, refreshed)
            }
            status
        }
    }

    private fun reply(
        queryId: String,
        status: String,
        message: String,
        view: DashboardWidgetView? = null,
    ): TelegramDeliveryResult {
        val payload =
            if (view == null) {
                json.writeValueAsString(mapOf("status" to status, "message" to message))
            } else {
                json.writeValueAsString(
                    mapOf(
                        "status" to status,
                        "message" to message,
                        "state" to json.readTree(publisher.state(view)),
                    ),
                )
            }
        return gateway.answerQuery(queryId, payload)
    }
}
