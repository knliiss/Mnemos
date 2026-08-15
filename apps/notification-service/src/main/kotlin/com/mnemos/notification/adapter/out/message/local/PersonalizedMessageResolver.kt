package com.mnemos.notification.adapter.out.message.local

import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.DungeonNotificationPayload
import com.mnemos.notification.application.notification.model.GlobalNotificationPayload
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class PersonalizedMessageResolver {
    fun resolve(
        item: NotificationItem,
        pack: ActiveNotificationPack,
    ): ResolvedNotification {
        val payload = item.payload
        val template = pack.templates.getValue(payload.type)
        val values =
            when (payload) {
                is ItemDropNotificationPayload ->
                    mapOf(
                        "player" to payload.droppedFor,
                        "item" to payload.itemName,
                        "rarity" to payload.itemRarity.name.lowercase(Locale.ROOT),
                        "itemType" to payload.itemType.name.lowercase(Locale.ROOT),
                    )

                is BoosterNotificationPayload ->
                    mapOf(
                        "player" to payload.activatedBy,
                        "booster" to payload.boosterType.name.lowercase(Locale.ROOT),
                    )

                is DungeonNotificationPayload ->
                    mapOf(
                        "dungeon" to payload.dungeonType.name.lowercase(Locale.ROOT),
                    )

                is RaidNotificationPayload ->
                    mapOf(
                        "locations" to payload.locations.sorted().joinToString(", "),
                    )

                is GlobalNotificationPayload ->
                    mapOf(
                        "event" to payload.eventType.name.lowercase(Locale.ROOT),
                    )
            }

        return ResolvedNotification(
            type = payload.type,
            soloTitle = template.withEmoji(template.soloTitle.render(values)),
            soloBody = template.soloBody.render(values),
            compact = template.withEmoji(template.compact.render(values)),
            aggregateTitle = template.withEmoji(TelegramHtmlEscaper.escape(template.aggregateTitle)),
        )
    }

    private fun String.render(values: Map<String, String>): String {
        val escapedTemplate = TelegramHtmlEscaper.escape(this)

        return PLACEHOLDER.replace(escapedTemplate) { match ->
            val name = match.groupValues[1]
            val value =
                requireNotNull(values[name]) {
                    "Notification template value is missing for placeholder {$name}"
                }

            TelegramHtmlEscaper.escape(value)
        }
    }

    private fun NotificationPackTemplate.withEmoji(value: String): String =
        if (emoji.isNullOrBlank()) {
            value
        } else {
            "${TelegramHtmlEscaper.escape(emoji)} $value"
        }

    private companion object {
        val PLACEHOLDER = Regex("\\{([a-zA-Z][a-zA-Z0-9_]*)}")
    }
}
