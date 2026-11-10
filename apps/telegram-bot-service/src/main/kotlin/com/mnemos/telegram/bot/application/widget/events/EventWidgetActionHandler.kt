package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.port.out.EventWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
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
class EventWidgetActionHandler(
    private val snapshots: EventWidgetSnapshotQuery,
    private val opener: OpenEventWidgetUseCase,
    private val publisher: EventWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    private val textInput: StartWidgetTextInputUseCase,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.EVENTS

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val telegramId = context.actorId
        val filter = EventHistoryFilter.entries.getOrNull(session.page)
            ?: return responder.reply(context.queryId, "EXPIRED", "Відкрийте стрічку заново")
        if (context.data == "w1:evt:search") {
            val answer = responder.reply(context.queryId, "INPUT", "Відповідайте на підказку в чаті")
            if (answer == TelegramDeliveryResult.Delivered) {
                runCatching { textInput.start(session, WidgetTextInputPurpose.EVENT_SEARCH) }
            }
            return answer
        }

        val old =
            (snapshots.load(telegramId, filter, session.cursor) as? EventWidgetSnapshotResult.Available)?.view
                ?: return responder.reply(context.queryId, "UNAVAILABLE", "Стрічка недоступна")

        if (context.data.startsWith("w1:evt:next:")) {
            val cursor =
                runCatching { UUID.fromString(context.data.removePrefix("w1:evt:next:")) }.getOrNull()
            if (cursor == null || cursor != old.nextCursor) {
                return responder.reply(context.queryId, "CONFLICT", "Сторінка змінилася, оновіть віджет")
            }
            val result = opener.execute(telegramId, filter, cursor)
            return responder.reply(
                context.queryId,
                if (result == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (result == TelegramDeliveryResult.Delivered) "Наступні події відкрито" else "Не вдалося відкрити події",
            )
        }

        val requestedFilter =
            when {
                context.data.startsWith("w1:evt:filter:") ->
                    EventHistoryFilter.entries.firstOrNull {
                        it.name == context.data.removePrefix("w1:evt:filter:")
                    }
                else -> null
            }

        val nextFilter =
            when {
                context.data == "w1:evt:refresh" -> filter
                requestedFilter != null -> requestedFilter
                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }
        val nextCursor = if (requestedFilter != null) null else session.cursor
        val fresh =
            (snapshots.load(telegramId, nextFilter, nextCursor) as? EventWidgetSnapshotResult.Available)?.view
                ?: return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося оновити події")

        val result = responder.reply(context.queryId, "OK", "Стрічку оновлено", publisher.state(fresh))
        if (result == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = nextFilter.ordinal, cursor = nextCursor))
            publisher.update(telegramId, context.messageId, fresh)
        }
        return result
    }
}
