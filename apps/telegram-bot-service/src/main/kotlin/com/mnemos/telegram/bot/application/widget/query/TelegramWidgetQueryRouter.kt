package com.mnemos.telegram.bot.application.widget.query

import com.mnemos.telegram.bot.application.identity.TelegramPrivateActorPolicy
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramWidgetQueryRouter(
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    handlers: List<TelegramWidgetActionHandler>,
) {
    private val handlersByScreen = handlers.associateBy { it.screen }

    init {
        require(handlersByScreen.size == handlers.size) { "Only one widget action handler per screen is allowed" }
    }

    fun route(query: TelegramWidgetQueryContext): TelegramDeliveryResult {
        val actor =
            TelegramPrivateActorPolicy.resolve(
                isPrivateChat = query.isPrivate,
                chatId = query.chatId,
                actorId = query.actorId,
            )

        if (actor == null || query.isShared) {
            return responder.reply(query.queryId, "FORBIDDEN", "Недоступна дія")
        }

        if (query.type != "data" || query.data.toByteArray(Charsets.UTF_8).size !in 1..64) {
            return responder.reply(query.queryId, "UNSUPPORTED", "Непідтримуваний запит")
        }

        val session = sessions.find(actor, query.messageId)
        if (session == null) {
            return responder.reply(query.queryId, "EXPIRED", "Відкрийте новий віджет")
        }

        val handler =
            handlersByScreen[session.screen]
                ?: return responder.reply(query.queryId, "UNSUPPORTED", "Невідомий екран")

        return handler.handle(query, session)
    }
}
