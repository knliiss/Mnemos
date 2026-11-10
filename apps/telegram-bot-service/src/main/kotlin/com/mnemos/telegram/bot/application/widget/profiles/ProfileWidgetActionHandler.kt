package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionChangeResult
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionState
import com.mnemos.telegram.bot.application.profilereaction.ProfileReactionType
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
import com.mnemos.telegram.bot.application.widget.profileactivity.OpenProfileActivityWidgetUseCase
import com.mnemos.telegram.bot.application.widget.profilecomments.OpenProfileCommentsWidgetUseCase
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
    private val activity: OpenProfileActivityWidgetUseCase,
    private val comments: OpenProfileCommentsWidgetUseCase,
    private val changeReaction: ChangeProfileReactionUseCase,
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
        if (context.data.startsWith("w1:prof:react:")) {
            val profileId = session.selectedProfileId
                ?: return responder.reply(context.queryId, "CONFLICT", "Спочатку відкрийте профіль")
            if (current.selected?.accountId != profileId || current.reaction !is ProfileReactionState.Available) {
                return responder.reply(context.queryId, "UNAVAILABLE", "Реакції зараз недоступні")
            }
            val desired = when (context.data.removePrefix("w1:prof:react:")) {
                "like" -> ProfileReactionType.LIKE
                "dislike" -> ProfileReactionType.DISLIKE
                "remove" -> null
                else -> return responder.reply(context.queryId, "INVALID", "Невірна реакція")
            }
            val changed = changeReaction.execute(context.actorId, profileId, desired, context.queryId)
            if (changed != ProfileReactionChangeResult.APPLIED) {
                val message = when (changed) {
                    ProfileReactionChangeResult.ACCOUNT_REQUIRED -> "Спочатку оберіть активний акаунт"
                    ProfileReactionChangeResult.FORBIDDEN -> "Операцію заборонено"
                    ProfileReactionChangeResult.INVALID -> "Запит уже оброблено або дані змінилися"
                    ProfileReactionChangeResult.UNAVAILABLE -> "Реакції тимчасово недоступні"
                    ProfileReactionChangeResult.APPLIED -> "Готово"
                }
                return responder.reply(context.queryId, changed.name, message)
            }
            val fresh = (snapshots.load(context.actorId, session.search, session.page, profileId)
                as? ProfileWidgetSnapshotResult.Available)?.view
                ?: return responder.reply(context.queryId, "UNAVAILABLE", "Реакцію змінено, оновіть профіль")
            val reply = responder.reply(context.queryId, "OK", "Реакцію оновлено", publisher.state(fresh))
            if (reply == TelegramDeliveryResult.Delivered) {
                publisher.update(context.actorId, context.messageId, fresh)
            }
            return reply
        }
        if (context.data == "w1:prof:comments") {
            val profileId = session.selectedProfileId
                ?: return responder.reply(context.queryId, "CONFLICT", "Спочатку відкрийте профіль")
            val result = comments.execute(context.actorId, profileId)
            return responder.reply(
                context.queryId,
                if (result == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (result == TelegramDeliveryResult.Delivered) "Коментарі відкрито" else "Коментарі недоступні",
            )
        }
        if (context.data == "w1:prof:activity") {
            val profileId = session.selectedProfileId
                ?: return responder.reply(context.queryId, "CONFLICT", "Спочатку відкрийте профіль")
            val opened = activity.execute(context.actorId, profileId)
            return responder.reply(
                context.queryId,
                if (opened == TelegramDeliveryResult.Delivered) "OK" else "UNAVAILABLE",
                if (opened == TelegramDeliveryResult.Delivered) "Активність відкрито" else "Активність недоступна",
            )
        }

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
