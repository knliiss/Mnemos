package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.slf4j.LoggerFactory
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
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
class ProfileCommentsWidgetActionHandler(
    private val snapshots: ProfileCommentsWidgetSnapshotQuery,
    private val publisher: ProfileCommentsWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    private val composer: StartProfileCommentInputUseCase,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.PROFILE_COMMENTS

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        val profileId = session.selectedProfileId
            ?: return responder.reply(context.queryId, "INVALID", "Оберіть профіль")
        val current = when (val result = snapshots.load(context.actorId, profileId, session.page, session.cursor)) {
            is ProfileCommentsWidgetSnapshotResult.Available -> result.view
            ProfileCommentsWidgetSnapshotResult.NotFound ->
                return responder.reply(context.queryId, "NOT_FOUND", "Коментарі або профіль не знайдено")
            ProfileCommentsWidgetSnapshotResult.Unavailable ->
                return responder.reply(context.queryId, "UNAVAILABLE", "Коментарі тимчасово недоступні")
        }
        if (context.data == "w1:comments:compose") {
            val response = responder.reply(context.queryId, "INPUT", "Відповідайте на підказку в чаті")
            if (response == TelegramDeliveryResult.Delivered) {
                runCatching { composer.execute(session) }
                    .onFailure { log.warn("Cannot start comment input for Telegram user {}", context.actorId, it) }
            }
            return response
        }
        val page: Int
        val root: UUID?
        when {
            context.data == "w1:comments:refresh" -> {
                page = current.page
                root = current.rootCommentId
            }
            context.data == "w1:comments:back" -> {
                if (current.rootCommentId == null) {
                    return responder.reply(context.queryId, "CONFLICT", "Ви вже у списку коментарів")
                }
                page = 0
                root = null
            }
            context.data.startsWith("w1:comments:thread:") -> {
                if (current.rootCommentId != null) {
                    return responder.reply(context.queryId, "CONFLICT", "Поверніться до коментарів")
                }
                val target = context.data.removePrefix("w1:comments:thread:").split(':')
                if (target.size != 2) return responder.reply(context.queryId, "INVALID", "Невірний коментар")
                val index = target[0].toIntOrNull()
                val item = index?.let { current.comments.getOrNull(it) }
                    ?: return responder.reply(context.queryId, "CONFLICT", "Оновіть список коментарів")
                if (item.deleted || item.replies <= 0 || item.id.toString().replace("-", "").take(12) != target[1]) {
                    return responder.reply(context.queryId, "CONFLICT", "Коментар змінився. Оновіть")
                }
                root = item.id
                page = 0
            }
            context.data.startsWith("w1:comments:next:") || context.data.startsWith("w1:comments:prev:") -> {
                val expected = context.data.substringAfterLast(':').toIntOrNull()
                if (expected != session.page) {
                    return responder.reply(context.queryId, "CONFLICT", "Сторінка змінилася. Оновіть")
                }
                page = (session.page + if (context.data.startsWith("w1:comments:next:")) 1 else -1)
                    .coerceIn(0, current.totalPages - 1)
                root = current.rootCommentId
            }
            else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
        }
        val fresh = when (val result = snapshots.load(context.actorId, profileId, page, root)) {
            is ProfileCommentsWidgetSnapshotResult.Available -> result.view
            ProfileCommentsWidgetSnapshotResult.NotFound ->
                return responder.reply(context.queryId, "NOT_FOUND", "Коментарі не знайдено")
            ProfileCommentsWidgetSnapshotResult.Unavailable ->
                return responder.reply(context.queryId, "UNAVAILABLE", "Коментарі недоступні")
        }
        val answer = responder.reply(context.queryId, "OK", "Коментарі оновлено", publisher.state(fresh))
        if (answer == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.page, cursor = root))
            publisher.update(context.actorId, context.messageId, fresh)
        }
        return answer
    }

    private companion object {
        val log = LoggerFactory.getLogger(ProfileCommentsWidgetActionHandler::class.java)
    }
}
