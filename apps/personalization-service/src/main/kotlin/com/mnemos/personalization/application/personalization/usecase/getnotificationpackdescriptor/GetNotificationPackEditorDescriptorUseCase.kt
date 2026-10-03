package com.mnemos.personalization.application.personalization.usecase.getnotificationpackdescriptor

import com.mnemos.personalization.application.personalization.builtin.RussianNotificationValues
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
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
            valueMappings = RussianNotificationValues.all,
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
                    variable(
                        key = key,
                        sampleValue = sampleValues.getValue(key),
                    )
                },
            sampleValues = sampleValues,
            conditionFields = conditionFields(type),
            defaults = defaults(type),
        )
    }

    private fun variable(
        key: String,
        sampleValue: String,
    ): NotificationPackEditorDescriptor.Variable {
        val valueKey = NotificationPackValueKey.forPlaceholder(key)
        val sampleForms = valueKey?.let(::sampleForms)
        val forms =
            buildList {
                add(
                    NotificationPackEditorDescriptor.VariableForm(
                        key = DEFAULT_FORM,
                        placeholder = "{$key}",
                        sampleValue = sampleForms?.default ?: sampleValue,
                    ),
                )

                sampleForms?.forms?.forEach { (form, value) ->
                    add(
                        NotificationPackEditorDescriptor.VariableForm(
                            key = form,
                            placeholder = "{$key.$form}",
                            sampleValue = value,
                        ),
                    )
                }
            }

        return NotificationPackEditorDescriptor.Variable(
            key = key,
            sampleValue = sampleValue,
            forms = forms,
        )
    }

    private fun conditionFields(type: NotificationPackTemplateType): List<NotificationPackEditorDescriptor.ConditionField> =
        NotificationPackVariantField.entries
            .filter { field -> field.templateType == type }
            .map { field ->
                NotificationPackEditorDescriptor.ConditionField(
                    field = field,
                    placeholder = field.valueKey.placeholder,
                    values =
                        field.allowedValues
                            .sorted()
                            .map { rawValue ->
                                NotificationPackEditorDescriptor.ConditionValue(
                                    value = rawValue,
                                    sampleValue = displayValue(field.valueKey, rawValue),
                                )
                            },
                )
            }

    private fun sampleForms(valueKey: NotificationPackValueKey): NotificationValueForms? {
        val rawValue = sampleRawValues[valueKey] ?: return null

        return RussianNotificationValues.all[valueKey]?.get(rawValue)
    }

    private fun displayValue(
        valueKey: NotificationPackValueKey,
        rawValue: String,
    ): String = RussianNotificationValues.all[valueKey]?.get(rawValue)?.default ?: rawValue

    private fun sampleValues(type: NotificationPackTemplateType): Map<String, String> =
        when (type) {
            NotificationPackTemplateType.ITEM_DROP ->
                linkedMapOf(
                    "player" to "north",
                    "item" to "Вечный клинок",
                    "rarity" to displayValue(NotificationPackValueKey.ITEM_RARITY, "MYTHICAL"),
                    "itemType" to displayValue(NotificationPackValueKey.ITEM_TYPE, "SWORD"),
                )

            NotificationPackTemplateType.BOOSTER ->
                linkedMapOf(
                    "player" to "vex",
                    "booster" to displayValue(NotificationPackValueKey.BOOSTER, "LUCK"),
                )

            NotificationPackTemplateType.DUNGEON ->
                linkedMapOf(
                    "dungeon" to displayValue(NotificationPackValueKey.DUNGEON, "EXPERT"),
                )

            NotificationPackTemplateType.RAID ->
                linkedMapOf(
                    "locations" to "2, 4, 7",
                )

            NotificationPackTemplateType.GLOBAL ->
                linkedMapOf(
                    "event" to displayValue(NotificationPackValueKey.GLOBAL_EVENT, "ECLIPSE"),
                )

            NotificationPackTemplateType.PING ->
                linkedMapOf(
                    "player" to "TranZorXx",
                    "message" to "MVP ┃ TranZorXx [#401] » @knaliz привет как дела?",
                    "target" to "knaliz",
                    "text" to "@knaliz привет как дела?",
                )

            NotificationPackTemplateType.PLAYER_OFFLINE ->
                linkedMapOf(
                    "player" to "KoTuK_PvP",
                    "realm" to "SAO-4",
                    "lastSeen" to "00:37",
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
                    soloBody = "{player} активировал {booster.accusative}.",
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

            NotificationPackTemplateType.PING ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "🔔",
                    soloTitle = "{player} упомянул вас",
                    soloBody = "{message}",
                    compact = "{player} → @{target}",
                    aggregateTitle = "Упоминания",
                )

            NotificationPackTemplateType.PLAYER_OFFLINE ->
                NotificationPackEditorDescriptor.Defaults(
                    emoji = "🌙",
                    soloTitle = "Игрок вышел",
                    soloBody = "{player} вышел из сети. Последний реалм: {realm}.",
                    compact = "{player} вышел из сети",
                    aggregateTitle = "Игроки вышли из сети",
                )
        }

    private companion object {
        const val MAX_TEXT_LENGTH = 512
        const val MAX_EMOJI_LENGTH = 16
        const val DEFAULT_FORM = "default"

        val sampleRawValues =
            mapOf(
                NotificationPackValueKey.ITEM_RARITY to "MYTHICAL",
                NotificationPackValueKey.ITEM_TYPE to "SWORD",
                NotificationPackValueKey.BOOSTER to "LUCK",
                NotificationPackValueKey.DUNGEON to "EXPERT",
                NotificationPackValueKey.GLOBAL_EVENT to "ECLIPSE",
            )
    }
}
