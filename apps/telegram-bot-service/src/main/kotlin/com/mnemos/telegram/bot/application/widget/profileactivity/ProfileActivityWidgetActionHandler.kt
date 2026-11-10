package com.mnemos.telegram.bot.application.widget.profileactivity

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profileactivity.port.out.ProfileActivityWidgetPublisher
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
class ProfileActivityWidgetActionHandler(
    private val snapshots: ProfileActivitySnapshotQuery,
    private val publisher: ProfileActivityWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen: TelegramWidgetScreen = TelegramWidgetScreen.PROFILE_ACTIVITY

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        val selected = session.selectedProfileId
            ?: return responder.reply(context.queryId, "INVALID", "Відкрийте активність із профілю")

        val after: UUID?
        when {
            context.data == "w1:activity:refresh" -> after = session.cursor
            context.data == "w1:activity:first" -> after = null
            context.data.startsWith("w1:activity:next:") -> {
                val expected = runCatching {
                    UUID.fromString(context.data.removePrefix("w1:activity:next:"))
                }.getOrNull() ?: return responder.reply(context.queryId, "INVALID", "Невірна сторінка")
                val current = snapshots.load(context.actorId, selected, session.cursor)
                    as? ProfileActivitySnapshotResult.Available
                    ?: return responder.reply(context.queryId, "UNAVAILABLE", "Активність недоступна")
                if (current.view.nextCursor != expected) {
                    return responder.reply(context.queryId, "CONFLICT", "Оновіть список активності")
                }
                after = expected
            }
            else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
        }

        val fresh = when (val result = snapshots.load(context.actorId, selected, after)) {
            is ProfileActivitySnapshotResult.Available -> result.view
            ProfileActivitySnapshotResult.NotFound ->
                return responder.reply(context.queryId, "NOT_FOUND", "Профіль не знайдено")
            ProfileActivitySnapshotResult.Unavailable ->
                return responder.reply(context.queryId, "UNAVAILABLE", "Активність недоступна")
        }
        val response = responder.reply(context.queryId, "OK", "Активність оновлено", publisher.state(fresh))
        if (response == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(cursor = after))
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return response
    }
}
