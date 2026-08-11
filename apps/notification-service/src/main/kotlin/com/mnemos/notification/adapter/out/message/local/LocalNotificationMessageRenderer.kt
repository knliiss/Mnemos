package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.telegram.start.TelegramStartContext
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "mnemos.notification.message-pack",
    name = ["mode"],
    havingValue = "LOCAL",
    matchIfMissing = true,
)
class LocalNotificationMessageRenderer(
    private val resolver: StandardMessageResolver,
) : NotificationMessageRenderer {
    override fun renderNotification(
        userId: UserId,
        items: List<NotificationItem>,
    ): RenderedMessage {
        require(items.isNotEmpty())

        val resolved = items.map(resolver::resolve)

        return RenderedMessage(
            text =
                if (resolved.size == 1) {
                    renderSolo(resolved.single())
                } else {
                    renderAggregated(resolved)
                },
        )
    }

    override fun renderStart(context: TelegramStartContext): RenderedMessage =
        RenderedMessage(
            text =
                "👋 <b>Добро пожаловать в Mnemos</b>\n\n" +
                    "Следите за важными событиями SAO подписками прямо в Mini App.",
            actions =
                listOf(
                    MessageAction.OpenMiniApp("Открыть Mnemos"),
                ),
        )

    private fun renderSolo(notification: ResolvedNotification): String = "${notification.soloTitle}\n${notification.soloBody}"

    private fun renderAggregated(notifications: List<ResolvedNotification>): String {
        val header = resolveAggregateHeader(notifications)

        return buildString {
            append(header)

            notifications.forEach { notification ->
                val entry = "\n\n${notification.compact}"

                if (length + entry.length > MAX_MESSAGE_LENGTH - TRUNCATION_SUFFIX.length) {
                    append(TRUNCATION_SUFFIX)
                    return@buildString
                }

                append(entry)
            }
        }
    }

    private fun resolveAggregateHeader(notifications: List<ResolvedNotification>): String {
        val types = notifications.map { it.type }.toSet()

        if (types.size > 1) {
            return "🔔 <b>Сразу несколько событий</b>"
        }

        return when (types.single()) {
            NotificationType.ITEM_DROP ->
                "🎁 <b>Новые находки</b>"

            NotificationType.BOOSTER ->
                "⚡ <b>Активированы бустеры</b>"

            NotificationType.DUNGEON ->
                "🏰 <b>Открылись подземелья</b>"

            NotificationType.RAID ->
                "⚔️ <b>Начались рейды</b>"

            NotificationType.GLOBAL ->
                "🌍 <b>События мира</b>"
        }
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 4096
        const val TRUNCATION_SUFFIX = "\n\n…и ещё несколько событий"
    }
}
