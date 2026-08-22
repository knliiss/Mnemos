package com.mnemos.personalization.application.personalization.usecase.getnotificationpackdescriptor

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import org.springframework.stereotype.Service

@Service
class GetNotificationPackEditorDescriptorUseCase {
    fun execute(): NotificationPackEditorDescriptor =
        NotificationPackEditorDescriptor(
            mixedAggregateTitleDefault = "Сразу несколько событий",
            maxTextLength = MAX_TEXT_LENGTH,
            maxEmojiLength = MAX_EMOJI_LENGTH,
            fields = fields(),
            templates = NotificationPackTemplateType.entries.map(::template),
        )

    private fun fields(): List<NotificationPackEditorDescriptor.Field> =
        listOf(
            NotificationPackEditorDescriptor.Field(
                key = NotificationPackEditorDescriptor.FieldKey.SOLO_TITLE,
                variablesAllowed = true,
            ),
            NotificationPackEditorDescriptor.Field(
                key = NotificationPackEditorDescriptor.FieldKey.SOLO_BODY,
                variablesAllowed = true,
            ),
            NotificationPackEditorDescriptor.Field(
                key = NotificationPackEditorDescriptor.FieldKey.COMPACT,
                variablesAllowed = true,
            ),
            NotificationPackEditorDescriptor.Field(
                key = NotificationPackEditorDescriptor.FieldKey.AGGREGATE_TITLE,
                variablesAllowed = false,
            ),
        )

    private fun template(type: NotificationPackTemplateType): NotificationPackEditorDescriptor.Template {
        val sampleValues = sampleValues(type)

        return NotificationPackEditorDescriptor.Template(
            type = type,
            variables =
                type.allowedPlaceholders.map { key ->
                    NotificationPackEditorDescriptor.Variable(
                        key = key,
                        sampleValue = sampleValues.getValue(key),
                    )
                },
            sampleValues = sampleValues,
            defaults = defaults(type),
        )
    }

    private fun sampleValues(type: NotificationPackTemplateType): Map<String, String> =
        when (type) {
            NotificationPackTemplateType.ITEM_DROP ->
                linkedMapOf(
                    "player" to "north",
                    "item" to "Вечный клинок",
                    "rarity" to "mythical",
                    "itemType" to "sword",
                )

            NotificationPackTemplateType.BOOSTER ->
                linkedMapOf(
                    "player" to "vex",
                    "booster" to "luck",
                )

            NotificationPackTemplateType.DUNGEON ->
                linkedMapOf(
                    "dungeon" to "expert",
                )

            NotificationPackTemplateType.RAID ->
                linkedMapOf(
                    "locations" to "2, 4, 7",
                )

            NotificationPackTemplateType.GLOBAL ->
                linkedMapOf(
                    "event" to "eclipse",
                )
        }

    private fun defaults(type: NotificationPackTemplateType): NotificationPackEditorDescriptor.Defaults =
        when (type) {
            NotificationPackTemplateType.ITEM_DROP ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "🎁",
                    soloTitle = "Новая находка",
                    soloBody = "{player} выбил «{item}» ({rarity}, {itemType}).",
                    compact = "{player} — «{item}»",
                    aggregateTitle = "Новые находки",
                )

            NotificationPackTemplateType.BOOSTER ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "⚡",
                    soloTitle = "Бустер активирован",
                    soloBody = "{player} активировал {booster}.",
                    compact = "{booster} — {player}",
                    aggregateTitle = "Активированы бустеры",
                )

            NotificationPackTemplateType.DUNGEON ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "🏰",
                    soloTitle = "Подземелье открыто",
                    soloBody = "{dungeon} уже доступно.",
                    compact = "{dungeon} открыто",
                    aggregateTitle = "Открылись подземелья",
                )

            NotificationPackTemplateType.RAID ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "⚔️",
                    soloTitle = "Начался рейд",
                    soloBody = "Локации: {locations}.",
                    compact = "Рейд — {locations}",
                    aggregateTitle = "Начались рейды",
                )

            NotificationPackTemplateType.GLOBAL ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "🌍",
                    soloTitle = "Событие мира",
                    soloBody = "Началось событие {event}.",
                    compact = "{event}",
                    aggregateTitle = "События мира",
                )
        }

    private companion object {
        const val MAX_TEXT_LENGTH = 512
        const val MAX_EMOJI_LENGTH = 16
    }
}
