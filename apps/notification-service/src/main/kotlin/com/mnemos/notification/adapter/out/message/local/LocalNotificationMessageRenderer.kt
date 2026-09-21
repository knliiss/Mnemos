package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.notification.localization.model.NotificationLocalizationContext
import com.mnemos.notification.application.notification.localization.service.NotificationLocalizationResolver
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
    private val localizationResolver: NotificationLocalizationResolver,
) : NotificationMessageRenderer {
    override fun renderNotification(
        userId: UserId,
        items: List<NotificationItem>,
    ): RenderedMessage {
        require(items.isNotEmpty())

        val deduplicatedItems = deduplicateBoosterItems(items)
        val activePack = activeNotificationPackRepository.find(userId)
        val localization = localizationResolver.resolve(userId)
        val resolution = resolveNotifications(userId, deduplicatedItems, activePack, localization)

        return RenderedMessage(
            text =
                if (resolution.notifications.size == 1) {
                    renderSolo(resolution.notifications.single())
                } else {
                    renderAggregated(
                        notifications = resolution.notifications,
                        pack = resolution.pack,
                        language = localization.language,
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
        localization: NotificationLocalizationContext,
    ): NotificationResolution {
        if (pack == null) {
            return NotificationResolution(
                notifications = items.map { item -> resolver.resolve(item, localization) },
                pack = null,
            )
        }

        return try {
            NotificationResolution(
                notifications =
                    items.map { item ->
                        personalizedResolver.resolve(
                            item = item,
                            pack = pack,
                            localization = localization,
                        )
                    },
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
                notifications = items.map { item -> resolver.resolve(item, localization) },
                pack = null,
            )
        }
    }

    private fun renderSolo(notification: ResolvedNotification): String = "${notification.soloTitle}\n${notification.soloBody}"

    private fun renderAggregated(
        notifications: List<ResolvedNotification>,
        pack: ActiveNotificationPack?,
        language: LocalizationLanguage,
    ): String {
        val header = resolveAggregateHeader(notifications, pack, language)
        val truncationSuffix = truncationSuffix(language)

        return buildString {
            append(header)

            notifications.forEach { notification ->
                val entry = "\n\n${notification.compact}"

                if (length + entry.length > MAX_MESSAGE_LENGTH - truncationSuffix.length) {
                    append(truncationSuffix)
                    return@buildString
                }

                append(entry)
            }
        }
    }

    private fun resolveAggregateHeader(
        notifications: List<ResolvedNotification>,
        pack: ActiveNotificationPack?,
        language: LocalizationLanguage,
    ): String {
        val types = notifications.map { it.type }.toSet()

        if (pack != null) {
            if (types.size > 1) {
                return "${resolvePersonalizedMixedAggregateLabel(language)} · ${TelegramHtmlEscaper.escape(pack.mixedAggregateTitle)}"
            }

            val variantIds = notifications.map { notification -> notification.variantId }.toSet()
            val first = notifications.first()
            val themedTitle =
                if (variantIds.size == 1) {
                    requireNotNull(first.aggregateTitle)
                } else {
                    requireNotNull(first.defaultAggregateTitle)
                }

            return "${resolvePersonalizedAggregateTypeLabel(types.single(), language)} · $themedTitle"
        }

        if (types.size > 1) {
            return when (language) {
                LocalizationLanguage.EN -> "🔔 <b>Several events at once</b>"
                LocalizationLanguage.RU -> "🔔 <b>Сразу несколько событий</b>"
                LocalizationLanguage.UK -> "🔔 <b>Одразу кілька подій</b>"
            }
        }

        return resolveStandardAggregateHeader(types.single(), language)
    }

    private fun resolvePersonalizedMixedAggregateLabel(language: LocalizationLanguage): String =
        when (language) {
            LocalizationLanguage.EN -> "🔔 <b>Several events</b>"
            LocalizationLanguage.RU -> "🔔 <b>Сразу несколько событий</b>"
            LocalizationLanguage.UK -> "🔔 <b>Одразу кілька подій</b>"
        }

    private fun resolvePersonalizedAggregateTypeLabel(
        type: NotificationType,
        language: LocalizationLanguage,
    ): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    NotificationType.ITEM_DROP -> "🎁 <b>Drops</b>"
                    NotificationType.BOOSTER -> "⚡ <b>Boosters</b>"
                    NotificationType.DUNGEON -> "🏰 <b>Dungeons</b>"
                    NotificationType.RAID -> "⚔️ <b>Raids</b>"
                    NotificationType.GLOBAL -> "🌍 <b>World events</b>"
                    NotificationType.PLAYER_OFFLINE -> "🌙 <b>Players offline</b>"
                }

            LocalizationLanguage.RU ->
                when (type) {
                    NotificationType.ITEM_DROP -> "🎁 <b>Дропы</b>"
                    NotificationType.BOOSTER -> "⚡ <b>Бустеры</b>"
                    NotificationType.DUNGEON -> "🏰 <b>Подземелья</b>"
                    NotificationType.RAID -> "⚔️ <b>Рейды</b>"
                    NotificationType.GLOBAL -> "🌍 <b>Глобальные события</b>"
                    NotificationType.PLAYER_OFFLINE -> "🌙 <b>Игроки офлайн</b>"
                }

            LocalizationLanguage.UK ->
                when (type) {
                    NotificationType.ITEM_DROP -> "🎁 <b>Дропи</b>"
                    NotificationType.BOOSTER -> "⚡ <b>Бустери</b>"
                    NotificationType.DUNGEON -> "🏰 <b>Підземелля</b>"
                    NotificationType.RAID -> "⚔️ <b>Рейди</b>"
                    NotificationType.GLOBAL -> "🌍 <b>Глобальні події</b>"
                    NotificationType.PLAYER_OFFLINE -> "🌙 <b>Гравці офлайн</b>"
                }
        }

    private fun resolveStandardAggregateHeader(
        type: NotificationType,
        language: LocalizationLanguage,
    ): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    NotificationType.ITEM_DROP -> "🎉 <b>New items</b>"
                    NotificationType.BOOSTER -> "⚡ <b>Boosters activated</b>"
                    NotificationType.DUNGEON -> "🏰 <b>Dungeons opened</b>"
                    NotificationType.RAID -> "⚔️ <b>Raids started</b>"
                    NotificationType.GLOBAL -> "🌍 <b>World events</b>"
                    NotificationType.PLAYER_OFFLINE -> "🌙 <b>Players went offline</b>"
                }

            LocalizationLanguage.RU ->
                when (type) {
                    NotificationType.ITEM_DROP -> "🎉 <b>Новые предметы</b>"
                    NotificationType.BOOSTER -> "⚡ <b>Активированы бустеры</b>"
                    NotificationType.DUNGEON -> "🏰 <b>Открылись подземелья</b>"
                    NotificationType.RAID -> "⚔️ <b>Начались рейды</b>"
                    NotificationType.GLOBAL -> "🌍 <b>События мира</b>"
                    NotificationType.PLAYER_OFFLINE -> "🌙 <b>Игроки вышли из игры</b>"
                }

            LocalizationLanguage.UK ->
                when (type) {
                    NotificationType.ITEM_DROP -> "🎉 <b>Нові предмети</b>"
                    NotificationType.BOOSTER -> "⚡ <b>Бустери активовано</b>"
                    NotificationType.DUNGEON -> "🏰 <b>Відкрилися підземелля</b>"
                    NotificationType.RAID -> "⚔️ <b>Почалися рейди</b>"
                    NotificationType.GLOBAL -> "🌍 <b>Події світу</b>"
                    NotificationType.PLAYER_OFFLINE -> "🌙 <b>Гравці вийшли з гри</b>"
                }
        }

    private fun truncationSuffix(language: LocalizationLanguage): String =
        when (language) {
            LocalizationLanguage.EN -> "\n\n…and several more events"
            LocalizationLanguage.RU -> "\n\n…и ещё несколько событий"
            LocalizationLanguage.UK -> "\n\n…і ще кілька подій"
        }

    private data class NotificationResolution(
        val notifications: List<ResolvedNotification>,
        val pack: ActiveNotificationPack?,
    )

    private companion object {
        const val MAX_MESSAGE_LENGTH = 4096

        val logger = LoggerFactory.getLogger(LocalNotificationMessageRenderer::class.java)
    }
}
