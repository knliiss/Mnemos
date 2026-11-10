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
    private val deletion: DeleteProfileCommentUseCase,
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
        if (context.data == "w1:comments:delete-cancel") {
            deletion.cancel(session)
            return responder.reply(context.queryId, "OK", "Видалення скасовано", publisher.state(current))
        }
        if (context.data.startsWith("w1:comments:edit:")) {
            val item = resolveItem(current, context.data.removePrefix("w1:comments:edit:"))
                ?: return responder.reply(context.queryId, "CONFLICT", "Коментар змінився. Оновіть")
            if (!item.canEdit || item.deleted) {
                return responder.reply(context.queryId, "FORBIDDEN", "Редагування цього коментаря недоступне")
            }
            val ack = responder.reply(context.queryId, "INPUT", "Напишіть новий текст у відповідь на підказку")
            if (ack == TelegramDeliveryResult.Delivered) {
                runCatching { composer.edit(session, item.id) }
                    .onSuccess { result ->
                        if (result != TelegramDeliveryResult.Delivered) {
                            publisher.update(context.actorId, context.messageId, current.copy(
                                feedback = "Не вдалося відкрити введення. Перевірте активний акаунт",
                            ))
                        }
                    }
                    .onFailure { log.warn("Cannot open edit-comment input for Telegram user {}", context.actorId, it) }
            }
            return ack
        }
        if (context.data.startsWith("w1:comments:delete:") ||
            context.data.startsWith("w1:comments:delete-confirm:")
        ) {
            val confirming = context.data.startsWith("w1:comments:delete-confirm:")
            val raw = context.data.removePrefix(
                if (confirming) "w1:comments:delete-confirm:" else "w1:comments:delete:",
            )
            val item = resolveItem(current, raw)
                ?: return responder.reply(context.queryId, "CONFLICT", "Коментар змінився. Оновіть")
            if (!item.canDelete || item.deleted) {
                return responder.reply(context.queryId, "FORBIDDEN", "Видалення цього коментаря недоступне")
            }
            val result = if (confirming) deletion.confirm(session, item.id)
                else deletion.request(session, item.id)
            return when (result) {
                ProfileCommentRemovalResult.CONFIRM_REQUIRED -> {
                    val index = current.comments.indexOfFirst { it.id == item.id }
                    responder.reply(
                        context.queryId, "CONFIRM", "Підтвердьте видалення коментаря",
                        publisher.state(current.copy(pendingDeleteIndex = index)),
                    )
                }
                ProfileCommentRemovalResult.APPLIED -> {
                    val fresh = (snapshots.load(context.actorId, profileId, session.page, session.cursor)
                        as? ProfileCommentsWidgetSnapshotResult.Available)?.view
                        ?: return responder.reply(context.queryId, "UNAVAILABLE", "Коментар видалено. Оновіть сторінку")
                    val response = responder.reply(
                        context.queryId, "OK", "Коментар видалено",
                        publisher.state(fresh.copy(feedback = "Коментар видалено")),
                    )
                    if (response == TelegramDeliveryResult.Delivered) {
                        publisher.update(context.actorId, context.messageId, fresh)
                    }
                    response
                }
                ProfileCommentRemovalResult.NOT_CONFIRMED ->
                    responder.reply(context.queryId, "CONFIRM_REQUIRED", "Потрібне нове підтвердження")
                ProfileCommentRemovalResult.ACCOUNT_REQUIRED ->
                    responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Оберіть активний акаунт")
                ProfileCommentRemovalResult.UNAVAILABLE ->
                    responder.reply(context.queryId, "UNAVAILABLE", "Видалення тимчасово недоступне")
                else -> responder.reply(context.queryId, "INVALID", "Дію відхилено")
            }
        }
        if (context.data == "w1:comments:compose") {
            val response = responder.reply(context.queryId, "INPUT", "Відповідайте на підказку в чаті")
            if (response == TelegramDeliveryResult.Delivered) {
                runCatching { composer.execute(session) }
                    .onSuccess { result ->
                        if (result != TelegramDeliveryResult.Delivered) {
                            publisher.update(context.actorId, context.messageId, current.copy(
                                feedback = "Не вдалося відкрити введення. Перевірте активний акаунт",
                            ))
                        }
                    }
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

    private fun resolveItem(view: ProfileCommentsWidgetView, raw: String): ProfileCommentWidgetItem? {
        val parts = raw.split(':')
        if (parts.size != 2) return null
        val index = parts[0].toIntOrNull() ?: return null
        val item = view.comments.getOrNull(index) ?: return null
        return item.takeIf { it.id.toString().replace("-", "").take(12) == parts[1] }
    }

    private companion object {
        val log = LoggerFactory.getLogger(ProfileCommentsWidgetActionHandler::class.java)
    }
}
