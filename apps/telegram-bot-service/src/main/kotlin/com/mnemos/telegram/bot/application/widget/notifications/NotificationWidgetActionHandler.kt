package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import com.mnemos.telegram.bot.application.widget.notifications.port.out.NotificationWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationWidgetActionHandler(
    private val snapshots: NotificationWidgetSnapshotQuery,
    private val subscriptions: NotificationSubscriptionGateway,
    private val publisher: NotificationWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.NOTIFICATIONS

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val telegramId = context.actorId
        val loaded = snapshots.load(telegramId, session.page)
        if (loaded !is NotificationWidgetSnapshotResult.Available) {
            return responder.reply(context.queryId, "UNAVAILABLE", "Сповіщення недоступні")
        }

        val current = loaded.view
        if (current.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте підписки знову")
        }

        var targetPage = session.page
        val message =
            when {
                context.data == "w1:sub:refresh" -> "Дані оновлено"
                context.data.startsWith("w1:next:") -> {
                    val expectedPage = context.data.removePrefix("w1:next:").toIntOrNull()
                    if (expectedPage != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    targetPage = (session.page + 1).coerceAtMost((current.totalPages - 1).coerceAtLeast(0))
                    "Наступна сторінка"
                }

                context.data.startsWith("w1:prev:") -> {
                    val expectedPage = context.data.removePrefix("w1:prev:").toIntOrNull()
                    if (expectedPage != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    targetPage = (session.page - 1).coerceAtLeast(0)
                    "Попередня сторінка"
                }

                context.data == "w1:ping:on" || context.data == "w1:ping:off" -> {
                    val desired = context.data == "w1:ping:on"
                    if (current.pingEnabled != desired && !subscriptions.setPingEnabled(telegramId, desired)) {
                        return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося змінити ping")
                    }
                    "Налаштування ping змінено"
                }

                context.data.startsWith("w1:del:") -> {
                    val id = runCatching { UUID.fromString(context.data.removePrefix("w1:del:")) }.getOrNull()
                    val visible = current.itemDrops.firstOrNull { item -> item.id == id }
                    if (visible == null) {
                        return responder.reply(context.queryId, "EXPIRED", "Оновіть список підписок")
                    }
                    if (!subscriptions.delete(telegramId, visible.id)) {
                        return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося видалити підписку")
                    }
                    "Підписку видалено"
                }

                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        var fresh =
            (snapshots.load(telegramId, targetPage) as? NotificationWidgetSnapshotResult.Available)?.view
        if (fresh != null && fresh.page >= fresh.totalPages && fresh.totalPages > 0) {
            targetPage = fresh.totalPages - 1
            fresh =
                (snapshots.load(telegramId, targetPage) as? NotificationWidgetSnapshotResult.Available)?.view
        }

        if (fresh == null) {
            return responder.reply(context.queryId, "OK", message)
        }

        val response = responder.reply(context.queryId, "OK", message, state = publisher.state(fresh))
        if (response == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.page))
            publisher.update(telegramId, context.messageId, fresh)
        }

        return response
    }
}
