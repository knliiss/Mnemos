package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
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

        val deduplicatedItems = deduplicateBoosterItems(items)
        val activePack = activeNotificationPackRepository.find(userId)
        val resolution = resolveNotifications(userId, deduplicatedItems, activePack)

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

    private fun deduplicateBoosterItems(items: List<NotificationItem>): List<NotificationItem> {
        val seenBoosters = mutableSetOf<BoosterNotificationPayload>()

        return items.filter { item ->
            val payload = item.payload

            payload !is BoosterNotificationPayload || seenBoosters.add(payload)
        }
    }

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
            if (types.size > 1) {
                return TelegramHtmlEscaper.escape(pack.mixedAggregateTitle)
            }

            val variantIds = notifications.map { notification -> notification.variantId }.toSet()
            val first = notifications.first()

            return if (variantIds.size == 1) {
                requireNotNull(first.aggregateTitle)
            } else {
                requireNotNull(first.defaultAggregateTitle)
            }
        }

        if (types.size > 1) {
            return "🔔 <b>Сразу несколько событий</b>"
        }

        return when (types.single()) {
            NotificationType.ITEM_DROP -> "🎁 <b>Новые находки</b>"
            NotificationType.BOOSTER -> "⚡ <b>Активированы бустеры</b>"
            NotificationType.DUNGEON -> "🏰 <b>Открылись подземелья</b>"
            NotificationType.RAID -> "⚔️ <b>Начались рейды</b>"
            NotificationType.GLOBAL -> "🌍 <b>События мира</b>"
            NotificationType.PLAYER_OFFLINE -> "🌙 <b>Игроки вышли из игры</b>"
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
