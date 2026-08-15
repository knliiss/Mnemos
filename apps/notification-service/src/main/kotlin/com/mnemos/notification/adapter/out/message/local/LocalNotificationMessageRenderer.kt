package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
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
    private val personalizedResolver: PersonalizedMessageResolver,
    private val activeNotificationPackRepository: ActiveNotificationPackRepository,
) : NotificationMessageRenderer {
    override fun renderNotification(
        userId: UserId,
        items: List<NotificationItem>,
    ): RenderedMessage {
        require(items.isNotEmpty())

        val pack = activeNotificationPackRepository.find(userId)
        val resolved =
            if (pack == null) {
                items.map(resolver::resolve)
            } else {
                items.map { item -> personalizedResolver.resolve(item, pack) }
            }

        return RenderedMessage(
            text =
                if (resolved.size == 1) {
                    renderSolo(resolved.single())
                } else {
                    renderAggregated(
                        notifications = resolved,
                        pack = pack,
                    )
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

    private fun renderAggregated(
        notifications: List<ResolvedNotification>,
        pack: ActiveNotificationPack?,
    ): String {
        val header = resolveAggregateHeader(notifications, pack)

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

    private fun resolveAggregateHeader(
        notifications: List<ResolvedNotification>,
        pack: ActiveNotificationPack?,
    ): String {
        val types = notifications.map { it.type }.toSet()

        if (pack != null) {
            return if (types.size > 1) {
                TelegramHtmlEscaper.escape(pack.mixedAggregateTitle)
            } else {
                requireNotNull(notifications.first().aggregateTitle)
            }
        }

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
