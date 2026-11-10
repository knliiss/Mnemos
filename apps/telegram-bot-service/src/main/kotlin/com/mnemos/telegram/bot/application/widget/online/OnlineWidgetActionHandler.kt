package com.mnemos.telegram.bot.application.widget.online

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.online.port.out.OnlineWidgetPublisher
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

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class OnlineWidgetActionHandler(
    private val snapshots: OnlineWidgetSnapshotQuery,
    private val publisher: OnlineWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    private val textInput: StartWidgetTextInputUseCase,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.ONLINE

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        if (context.data == "w1:on:search") {
            val answer = responder.reply(context.queryId, "INPUT", "Підказка буде в чаті. Якщо її немає, натисніть ще раз")
            if (answer == TelegramDeliveryResult.Delivered) {
                runCatching { textInput.start(session, WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH) }
            }
            return answer
        }

        val currentRealm = session.page.takeIf { it > 0 }
        val current = (snapshots.load(context.actorId, currentRealm) as? OnlineWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Дані SAO Online недоступні")

        val realmNumber =
            when {
                context.data == "w1:on:refresh" -> currentRealm
                context.data == "w1:on:list" -> null
                context.data.startsWith("w1:on:realm:") -> {
                    val candidate = context.data.removePrefix("w1:on:realm:").toIntOrNull()
                        ?: return responder.reply(context.queryId, "INVALID", "Невірний реалм")
                    if (current.selectedRealmNumber != null || current.realms.none { it.number == candidate }) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть список реалмів")
                    }
                    candidate
                }

                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        val fresh = (snapshots.load(context.actorId, realmNumber) as? OnlineWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Реалм зараз недоступний")

        val response = responder.reply(context.queryId, "OK", "SAO Online оновлено", publisher.state(fresh))
        if (response == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.selectedRealmNumber ?: 0))
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return response
    }
}
