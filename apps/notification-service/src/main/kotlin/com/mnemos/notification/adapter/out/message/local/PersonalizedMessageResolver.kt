package com.mnemos.notification.adapter.out.message.local

import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.DungeonNotificationPayload
import com.mnemos.notification.application.notification.model.GlobalNotificationPayload
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationPayload
import com.mnemos.notification.application.notification.model.PlayerOfflineNotificationPayload
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplateVariant
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import org.springframework.stereotype.Component

@Component
class PersonalizedMessageResolver {
    fun resolve(
        item: NotificationItem,
        pack: ActiveNotificationPack,
    ): ResolvedNotification {
        val payload = item.payload
        val template = pack.templates.getValue(payload.type)
        val variant = selectVariant(template, payload)
        val selected = SelectedTemplate.from(template, variant)
        val values = values(payload)

        return ResolvedNotification(
            type = payload.type,
            soloTitle = selected.withEmoji(render(selected.soloTitle, values, pack)),
            soloBody = render(selected.soloBody, values, pack),
            compact = selected.withEmoji(render(selected.compact, values, pack)),
            aggregateTitle = selected.withEmoji(TelegramHtmlEscaper.escape(selected.aggregateTitle)),
            defaultAggregateTitle = template.withEmoji(TelegramHtmlEscaper.escape(template.aggregateTitle)),
            variantId = variant?.id,
        )
    }

    private fun selectVariant(
        template: NotificationPackTemplate,
        payload: NotificationPayload,
    ): NotificationPackTemplateVariant? {
        val conditionValues = conditionValues(payload)

        return template.variants
            .filter { variant ->
                variant.conditions.all { condition ->
                    conditionValues[condition.field] == condition.value
                }
            }.maxByOrNull { variant -> variant.conditions.size }
    }

    private fun conditionValues(payload: NotificationPayload): Map<NotificationPackVariantField, String> =
        when (payload) {
            is ItemDropNotificationPayload ->
                mapOf(
                    NotificationPackVariantField.ITEM_RARITY to payload.itemRarity.name,
                    NotificationPackVariantField.ITEM_TYPE to payload.itemType.name,
                )

            is BoosterNotificationPayload ->
                mapOf(NotificationPackVariantField.BOOSTER to payload.boosterType.name)

            is DungeonNotificationPayload ->
                mapOf(NotificationPackVariantField.DUNGEON to payload.dungeonType.name)

            is RaidNotificationPayload -> emptyMap()

            is GlobalNotificationPayload ->
                mapOf(NotificationPackVariantField.GLOBAL_EVENT to payload.eventType.name)

            is PlayerOfflineNotificationPayload -> emptyMap()
        }

    private fun values(payload: NotificationPayload): Map<String, String> =
        when (payload) {
            is ItemDropNotificationPayload ->
                mapOf(
                    "player" to payload.droppedFor,
                    "item" to payload.itemName,
                    "rarity" to payload.itemRarity.name,
                    "itemType" to payload.itemType.name,
                )

            is BoosterNotificationPayload ->
                mapOf(
                    "player" to payload.activatedBy,
                    "booster" to payload.boosterType.name,
                )

            is DungeonNotificationPayload -> mapOf("dungeon" to payload.dungeonType.name)

            is RaidNotificationPayload -> mapOf("locations" to payload.locations.sorted().joinToString(", "))

            is GlobalNotificationPayload -> mapOf("event" to payload.eventType.name)

            is PlayerOfflineNotificationPayload ->
                mapOf(
                    "player" to payload.player,
                    "realm" to (payload.previousRealm ?: "—"),
                    "lastSeen" to (payload.lastSeenOnline?.toString() ?: "—"),
                )
        }

    private fun render(
        template: String,
        values: Map<String, String>,
        pack: ActiveNotificationPack,
    ): String {
        val escapedTemplate = TelegramHtmlEscaper.escape(template)

        return PLACEHOLDER.replace(escapedTemplate) { match ->
            val name = match.groupValues[1]
            val form = match.groupValues[2].ifBlank { null }
            val rawValue =
                requireNotNull(values[name]) {
                    "Notification template value is missing for placeholder {$name}"
                }
            val renderedValue = resolveValue(pack, name, rawValue, form)

            TelegramHtmlEscaper.escape(renderedValue)
        }
    }

    private fun resolveValue(
        pack: ActiveNotificationPack,
        placeholder: String,
        rawValue: String,
        form: String?,
    ): String {
        val valueKey = NotificationPackValueKey.forPlaceholder(placeholder)

        if (valueKey == null) {
            require(form == null) {
                "Notification placeholder {$placeholder} does not support named forms"
            }

            return rawValue
        }

        val packValue = pack.valueMappings[valueKey]?.get(rawValue)?.resolve(form)

        if (packValue != null) {
            return packValue
        }

        return StandardNotificationValueDictionary.resolve(valueKey, rawValue, form)
            ?: if (form == null || form == "default") {
                rawValue
            } else {
                error("Notification value $valueKey/$rawValue does not define form $form")
            }
    }

    private fun SelectedTemplate.withEmoji(value: String): String =
        if (emoji.isNullOrBlank()) {
            value
        } else {
            "${TelegramHtmlEscaper.escape(emoji)} $value"
        }

    private fun NotificationPackTemplate.withEmoji(value: String): String =
        if (emoji.isNullOrBlank()) {
            value
        } else {
            "${TelegramHtmlEscaper.escape(emoji)} $value"
        }

    private data class SelectedTemplate(
        val emoji: String?,
        val soloTitle: String,
        val soloBody: String,
        val compact: String,
        val aggregateTitle: String,
    ) {
        companion object {
            fun from(
                template: NotificationPackTemplate,
                variant: NotificationPackTemplateVariant?,
            ): SelectedTemplate =
                if (variant == null) {
                    SelectedTemplate(
                        emoji = template.emoji,
                        soloTitle = template.soloTitle,
                        soloBody = template.soloBody,
                        compact = template.compact,
                        aggregateTitle = template.aggregateTitle,
                    )
                } else {
                    SelectedTemplate(
                        emoji = variant.emoji,
                        soloTitle = variant.soloTitle,
                        soloBody = variant.soloBody,
                        compact = variant.compact,
                        aggregateTitle = variant.aggregateTitle,
                    )
                }
        }
    }

    private companion object {
        val PLACEHOLDER = Regex("\\{([a-zA-Z][a-zA-Z0-9_]*)(?:\\.([a-zA-Z][a-zA-Z0-9_]*))?}")
    }
}
