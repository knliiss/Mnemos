package com.mnemos.telegram.bot.application.widget.settings

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsMutationResult
import com.mnemos.telegram.bot.application.settings.port.out.SettingsGateway
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.application.widget.settings.port.out.SettingsWidgetPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class SettingsWidgetActionHandler(
    private val snapshots: SettingsWidgetSnapshotQuery,
    private val gateway: SettingsGateway,
    private val publisher: SettingsWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.SETTINGS

    override fun handle(context: TelegramWidgetQueryContext, session: TelegramWidgetSession): TelegramDeliveryResult {
        val current = (snapshots.load(context.actorId, session.page) as? SettingsWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Налаштування недоступні")

        var targetPage = session.page
        val result =
            when {
                context.data == "w1:settings:refresh" -> TelegramSettingsMutationResult.UPDATED
                context.data.startsWith("w1:settings:next:") || context.data.startsWith("w1:settings:prev:") -> {
                    val forward = context.data.startsWith("w1:settings:next:")
                    val expected = context.data.substringAfterLast(':').toIntOrNull()
                    if (expected != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть сторінку")
                    }
                    targetPage = (session.page + if (forward) 1 else -1).coerceIn(0, current.totalPages - 1)
                    TelegramSettingsMutationResult.UPDATED
                }
                context.data.startsWith("w1:settings:lang:") -> {
                    val language = TelegramSettingsLanguage.entries.firstOrNull {
                        it.name == context.data.removePrefix("w1:settings:lang:")
                    } ?: return responder.reply(context.queryId, "INVALID", "Невідома мова")
                    if (current.language == language) TelegramSettingsMutationResult.UPDATED
                    else gateway.updateLanguage(context.actorId, language)
                }
                context.data.startsWith("w1:settings:pack:") -> {
                    val id = runCatching { UUID.fromString(context.data.removePrefix("w1:settings:pack:")) }.getOrNull()
                        ?: return responder.reply(context.queryId, "INVALID", "Невідомий пакет")
                    if (current.packs.none { it.id == id }) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть список пакетів")
                    }
                    if (current.activePackId == id) TelegramSettingsMutationResult.UPDATED
                    else gateway.activateNotificationPack(context.actorId, id)
                }
                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        if (result != TelegramSettingsMutationResult.UPDATED) {
            return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося змінити налаштування")
        }

        val refreshed = (snapshots.load(context.actorId, targetPage) as? SettingsWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "OK", "Дію виконано. Оновіть налаштування")
        val answer = responder.reply(context.queryId, "OK", "Налаштування оновлено", publisher.state(refreshed))
        if (answer == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = refreshed.page))
            publisher.update(context.actorId, context.messageId, refreshed)
        }
        return answer
    }
}
