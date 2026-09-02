package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.telegram.bot.application.pack.model.NotificationPackAggregatePreviewMode
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackPreview
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplate
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateVariant

object NotificationPackPreviewFormatter {
    val templateTypes: List<NotificationPackTemplateType> =
        listOf(
            NotificationPackTemplateType.ITEM_DROP,
            NotificationPackTemplateType.BOOSTER,
            NotificationPackTemplateType.DUNGEON,
            NotificationPackTemplateType.RAID,
            NotificationPackTemplateType.GLOBAL,
            NotificationPackTemplateType.PLAYER_OFFLINE,
        )

    fun render(
        preview: TelegramNotificationPackPreview,
        selectedType: NotificationPackTemplateType,
        selectedVariantId: String?,
        aggregateMode: NotificationPackAggregatePreviewMode,
    ): NotificationPackRenderedPreview {
        val resolvedType =
            selectedType.takeIf(preview.templates::containsKey)
                ?: templateTypes.firstOrNull(preview.templates::containsKey)
                ?: error("Notification pack preview does not contain any supported templates")
        val template = requireNotNull(preview.templates[resolvedType])
        val selectedVariant = template.variants.firstOrNull { variant -> variant.id == selectedVariantId }
        val message = selectedVariant?.toMessageFields() ?: template.toMessageFields()
        val values = valuesForVariant(resolvedType, selectedVariant)
        val standardTitle =
            withEmoji(
                message.emoji,
                renderTemplate(
                    preview = preview,
                    template = message.soloTitle,
                    values = values,
                ),
            )
        val standardBody =
            renderTemplate(
                preview = preview,
                template = message.soloBody,
                values = values,
            )
        val aggregate =
            when (aggregateMode) {
                NotificationPackAggregatePreviewMode.SAME_TYPE ->
                    renderSameTypeAggregate(
                        preview = preview,
                        message = message,
                        values = values,
                    )

                NotificationPackAggregatePreviewMode.MIXED -> renderMixedAggregate(preview)
            }

        return NotificationPackRenderedPreview(
            selectedType = resolvedType,
            selectedVariant = selectedVariant,
            standardTitle = standardTitle,
            standardBody = standardBody,
            aggregateTitle = aggregate.title,
            aggregateLines = aggregate.lines,
        )
    }

    fun variantLabel(variant: TelegramNotificationTemplateVariant): String =
        variant.conditions
            .joinToString(" · ") { condition ->
                fallbackValueLabels[condition.field.valueKey]?.get(condition.value)
                    ?: condition.value
            }

    private fun renderSameTypeAggregate(
        preview: TelegramNotificationPackPreview,
        message: NotificationMessageFields,
        values: Map<String, String>,
    ): AggregatePreview =
        AggregatePreview(
            title = withEmoji(message.emoji, message.aggregateTitle),
            lines =
                listOf(
                    withEmoji(
                        message.emoji,
                        renderTemplate(
                            preview = preview,
                            template = message.compact,
                            values = values,
                        ),
                    ),
                ),
        )

    private fun renderMixedAggregate(preview: TelegramNotificationPackPreview): AggregatePreview {
        val lines =
            templateTypes
                .take(MIXED_PREVIEW_TEMPLATE_COUNT)
                .mapNotNull { type ->
                    val template = preview.templates[type] ?: return@mapNotNull null

                    withEmoji(
                        template.emoji,
                        renderTemplate(
                            preview = preview,
                            template = template.compact,
                            values = valuesForVariant(type, null),
                        ),
                    )
                }

        return AggregatePreview(
            title = preview.mixedAggregateTitle,
            lines = lines,
        )
    }

    private fun valuesForVariant(
        type: NotificationPackTemplateType,
        variant: TelegramNotificationTemplateVariant?,
    ): Map<String, String> {
        val values = previewValues(type).toMutableMap()

        variant?.conditions?.forEach { condition ->
            values[condition.field.valueKey.placeholder] = condition.value
        }

        return values
    }

    private fun previewValues(type: NotificationPackTemplateType): Map<String, String> =
        when (type) {
            NotificationPackTemplateType.ITEM_DROP ->
                mapOf(
                    "player" to "north",
                    "item" to "Вечный клинок",
                    "rarity" to "MYTHICAL",
                    "itemType" to "SWORD",
                )

            NotificationPackTemplateType.BOOSTER ->
                mapOf(
                    "player" to "vex",
                    "booster" to "LUCK",
                )

            NotificationPackTemplateType.DUNGEON -> mapOf("dungeon" to "EXPERT")
            NotificationPackTemplateType.RAID -> mapOf("locations" to "2 · 4 · 7")
            NotificationPackTemplateType.GLOBAL -> mapOf("event" to "ECLIPSE")
            NotificationPackTemplateType.PLAYER_OFFLINE ->
                mapOf(
                    "player" to "KoTuK_PvP",
                    "realm" to "SAO-4",
                    "lastSeen" to "00:37",
                )
        }

    private fun renderTemplate(
        preview: TelegramNotificationPackPreview,
        template: String,
        values: Map<String, String>,
    ): String =
        placeholderRegex.replace(template) { match ->
            val key = match.groupValues[1]
            val form = match.groupValues[2].ifBlank { null }
            val rawValue = values[key] ?: return@replace match.value
            val valueKey = NotificationPackValueKey.forPlaceholder(key) ?: return@replace rawValue
            val mapped = preview.valueMappings[valueKey]?.get(rawValue)

            if (mapped == null) {
                fallbackValueLabels[valueKey]?.get(rawValue) ?: rawValue
            } else if (form == null || form == DEFAULT_FORM) {
                mapped.default
            } else {
                mapped.forms[form] ?: mapped.default
            }
        }

    private fun TelegramNotificationTemplate.toMessageFields(): NotificationMessageFields =
        NotificationMessageFields(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun TelegramNotificationTemplateVariant.toMessageFields(): NotificationMessageFields =
        NotificationMessageFields(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun withEmoji(
        emoji: String?,
        value: String,
    ): String {
        val normalizedEmoji = emoji?.trim()

        return if (normalizedEmoji.isNullOrEmpty()) {
            value
        } else {
            "$normalizedEmoji $value"
        }
    }

    private data class NotificationMessageFields(
        val emoji: String?,
        val soloTitle: String,
        val soloBody: String,
        val compact: String,
        val aggregateTitle: String,
    )

    private data class AggregatePreview(
        val title: String,
        val lines: List<String>,
    )

    private const val MIXED_PREVIEW_TEMPLATE_COUNT = 3
    private const val DEFAULT_FORM = "default"

    private val placeholderRegex =
        Regex("\\{([a-zA-Z][a-zA-Z0-9_]*)(?:\\.([a-zA-Z][a-zA-Z0-9_]*))?}")

    private val fallbackValueLabels: Map<NotificationPackValueKey, Map<String, String>> =
        mapOf(
            NotificationPackValueKey.ITEM_RARITY to
                mapOf(
                    "MYTHICAL" to "Мифический",
                    "SECRET" to "Секретный",
                ),
            NotificationPackValueKey.ITEM_TYPE to
                mapOf(
                    "SWORD" to "меч",
                    "AURA" to "аура",
                    "PET" to "питомец",
                    "BOOK" to "книга",
                    "JEWELRY" to "реликвия",
                ),
            NotificationPackValueKey.BOOSTER to
                mapOf(
                    "LUCK" to "Удача",
                    "MONEY" to "Доход",
                    "DAMAGE" to "Урон",
                    "POWER" to "Сила",
                ),
            NotificationPackValueKey.DUNGEON to
                mapOf(
                    "EASY" to "Лёгкое подземелье",
                    "MEDIUM" to "Среднее подземелье",
                    "HARD" to "Сложное подземелье",
                    "EXPERT" to "Экспертное подземелье",
                ),
            NotificationPackValueKey.GLOBAL_EVENT to
                mapOf(
                    "DARKNESS" to "Тьма",
                    "MOON" to "Кровавая луна",
                    "ECLIPSE" to "Затмение",
                    "EXPLOSION" to "Взрыв",
                    "COMET_CHAOS" to "Кометный хаос",
                ),
        )
}

data class NotificationPackRenderedPreview(
    val selectedType: NotificationPackTemplateType,
    val selectedVariant: TelegramNotificationTemplateVariant?,
    val standardTitle: String,
    val standardBody: String,
    val aggregateTitle: String,
    val aggregateLines: List<String>,
)
