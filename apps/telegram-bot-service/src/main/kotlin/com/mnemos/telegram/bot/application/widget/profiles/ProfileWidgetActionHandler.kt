package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
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
class ProfileWidgetActionHandler(
    private val snapshots: ProfileWidgetSnapshotQuery,
    private val publisher: ProfileWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    private val input: StartWidgetTextInputUseCase,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.PROFILES

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        if (context.data == "w1:prof:search") {
            val answer = responder.reply(context.queryId, "INPUT", "Відповідайте на підказку пошуку в чаті")
            if (answer == TelegramDeliveryResult.Delivered) {
                runCatching { input.start(session, WidgetTextInputPurpose.PUBLIC_PROFILE_SEARCH) }
            }
            return answer
        }

        val current = (snapshots.load(context.actorId, session.search, session.page, session.selectedProfileId)
            as? ProfileWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Профілі зараз недоступні")
        var query = session.search
        var page = session.page
        var selected = session.selectedProfileId
        val message =
            when {
                context.data == "w1:prof:refresh" -> "Дані оновлено"
                context.data == "w1:prof:clear" -> {
                    query = null
                    page = 0
                    selected = null
                    "Пошук скинуто"
                }
                context.data == "w1:prof:back" -> {
                    selected = null
                    "Повернулися до результатів"
                }
                context.data.startsWith("w1:prof:open:") -> {
                    if (selected != null) {
                        return responder.reply(context.queryId, "CONFLICT", "Поверніться до пошуку")
                    }
                    val id = runCatching {
                        UUID.fromString(context.data.removePrefix("w1:prof:open:"))
                    }.getOrNull() ?: return responder.reply(context.queryId, "INVALID", "Невірний профіль")
                    if (current.profiles.none { it.accountId == id }) {
                        return responder.reply(context.queryId, "CONFLICT", "Профіль не входить до поточних результатів")
                    }
                    selected = id
                    "Профіль відкрито"
                }
                context.data.startsWith("w1:prof:next:") || context.data.startsWith("w1:prof:prev:") -> {
                    val expected = context.data.substringAfterLast(':').toIntOrNull()
                    if (selected != null || query == null || expected != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть результати")
                    }
                    val forward = context.data.startsWith("w1:prof:next:")
                    page = (session.page + if (forward) 1 else -1)
                        .coerceIn(0, current.totalPages - 1)
                    "Сторінку оновлено"
                }
                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        val fresh = when (val result = snapshots.load(context.actorId, query, page, selected)) {
            is ProfileWidgetSnapshotResult.Available -> result.view
            ProfileWidgetSnapshotResult.NotFound ->
                return responder.reply(context.queryId, "NOT_FOUND", "Профіль не знайдено")
            ProfileWidgetSnapshotResult.Unavailable ->
                return responder.reply(context.queryId, "UNAVAILABLE", "Профілі недоступні")
        }
        val answer = responder.reply(context.queryId, "OK", message, publisher.state(fresh))
        if (answer == TelegramDeliveryResult.Delivered) {
            sessions.save(
                session.copy(
                    page = fresh.page,
                    search = query,
                    selectedProfileId = selected,
                ),
            )
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return answer
    }
}
