package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountManagementWidgetActionHandler(
    private val snapshots: AccountManagementWidgetSnapshotQuery,
    private val publisher: AccountManagementWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.ACCOUNT_MANAGEMENT

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        val current = (snapshots.load(context.actorId, session.page) as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Керування акаунтом недоступне")
        if (session.accountId != current.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Активний акаунт змінився")
        }

        val targetPage =
            when {
                context.data == "w1:manage:refresh" -> session.page
                context.data.startsWith("w1:manage:next:") || context.data.startsWith("w1:manage:prev:") -> {
                    val expected = context.data.substringAfterLast(':').toIntOrNull()
                    if (expected != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    val next = if (context.data.startsWith("w1:manage:next:")) 1 else -1
                    (session.page + next).coerceIn(0, current.pages - 1)
                }
                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }
        val fresh = (snapshots.load(context.actorId, targetPage) as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Дані зараз недоступні")
        if (fresh.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Активний акаунт змінився")
        }
        val answer = responder.reply(context.queryId, "OK", "Дані оновлено", publisher.state(fresh))
        if (answer == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.page))
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return answer
    }
}
