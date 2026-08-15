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
import org.slf4j.LoggerFactory
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

        val activePack = activeNotificationPackRepository.find(userId)
        val resolution = resolveNotifications(userId, items, activePack)

        return RenderedMessage(
            text =
                if (resolution.notifications.size == 1) {
                    renderSolo(resolution.notifications.single())
                } else {
                    renderAggregated(
                        notifications = resolution.notifications,
                        pack = resolution.pack,
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

    private fun resolveNotifications(
        userId: UserId,
        items: List<NotificationItem>,
        pack: ActiveNotificationPack?,
    ): NotificationResolution {
        if (pack == null) {
            return NotificationResolution(
                notifications = items.map(resolver::resolve),
                pack = null,
            )
        }

        return try {
            NotificationResolution(
                notifications = items.map { item -> personalizedResolver.resolve(item, pack) },
                pack = pack,
            )
        } catch (exception: RuntimeException) {
            logger.warn(
                "Failed to render active notification pack, falling back to standard templates: userId={}, personalizationId={}",
                userId.id,
                pack.personalizationId,
                exception,
            )

            NotificationResolution(
                notifications = items.map(resolver::resolve),
                pack = null,
            )
        }
    }

    private fun renderSolo(notification: ResolvedNotification): String =
        "${notification.soloTitle}\n${notification.soloBody}"

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

    private data class NotificationResolution(
        val notifications: List<ResolvedNotification>,
        val pack: ActiveNotificationPack?,
    )

    private companion object {
        const val MAX_MESSAGE_LENGTH = 4096
        const val TRUNCATION_SUFFIX = "\n\n…и ещё несколько событий"

        val logger = LoggerFactory.getLogger(LocalNotificationMessageRenderer::class.java)
    }
}
