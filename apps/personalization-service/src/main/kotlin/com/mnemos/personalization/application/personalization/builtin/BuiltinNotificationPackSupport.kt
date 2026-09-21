package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateCondition
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
import com.mnemos.personalization.domain.personalization.model.PersonalizationCatalogKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

object BuiltinNotificationPackSupport {
    fun definition(
        key: String,
        name: String,
        pricePrime: Long,
        mixedAggregateTitle: String,
        itemDrop: NotificationTemplate,
        booster: NotificationTemplate,
        dungeon: NotificationTemplate,
        raid: NotificationTemplate,
        global: NotificationTemplate,
        playerOffline: NotificationTemplate = defaultPlayerOfflineTemplate(),
        valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueForms>> = RussianNotificationValues.all,
    ): BuiltinPersonalizationDefinition {
        val authoredVariants = BuiltinNotificationPackVariantSets.forPack(key)
        val itemDropVariants =
            normalizeItemDropVariants(
                key = key,
                baseEmoji = itemDrop.emoji,
                variants = authoredVariants?.itemDrop ?: itemDrop.variants,
            )
        val itemDropTemplate = itemDrop.copy(variants = itemDropVariants)
        val boosterTemplate =
            booster.copy(
                variants = authoredVariants?.booster ?: booster.variants,
            )
        val dungeonTemplate =
            dungeon.copy(
                variants = authoredVariants?.dungeon ?: dungeon.variants,
            )
        val baseGlobalTemplate =
            global.copy(
                variants = authoredVariants?.global ?: global.variants,
            )

        val cometCopy = BuiltinCometChaosNotificationVariants.forPack(key)
        val cometVariant =
            variant(
                id = "comet-chaos",
                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, COMET_CHAOS)),
                emoji = cometCopy.emoji,
                title = cometCopy.title,
                body = cometCopy.body,
                compact = cometCopy.compact,
                aggregateTitle = cometCopy.aggregateTitle,
            )
        val globalTemplate = baseGlobalTemplate.copy(variants = baseGlobalTemplate.variants + cometVariant)
        val normalizedValueMappings = withCometChaosValue(key, valueMappings)

        return BuiltinPersonalizationDefinition(
            catalogKey = PersonalizationCatalogKey(key),
            type = PersonalizationType.NOTIFICATION_PACK,
            name = name,
            content =
                PersonalizationContent.NotificationPack(
                    mixedAggregateTitle = mixedAggregateTitle,
                    templates =
                        mapOf(
                            NotificationTemplateKey.ITEM_DROP to itemDropTemplate,
                            NotificationTemplateKey.BOOSTER to boosterTemplate,
                            NotificationTemplateKey.DUNGEON to dungeonTemplate,
                            NotificationTemplateKey.RAID to raid,
                            NotificationTemplateKey.GLOBAL to globalTemplate,
                            NotificationTemplateKey.PLAYER_OFFLINE to playerOffline,
                        ),
                    valueMappings = normalizedValueMappings,
                ),
            price =
                PersonalizationPrice(
                    currency = Currency.PRIME,
                    amount = pricePrime,
                ),
        )
    }

    fun template(
        emoji: String?,
        title: String,
        body: String,
        compact: String,
        aggregateTitle: String,
        variants: List<NotificationTemplateVariant> = emptyList(),
    ): NotificationTemplate =
        NotificationTemplate(
            emoji = emoji,
            soloTitle = title,
            soloBody = body,
            compact = compact,
            aggregateTitle = aggregateTitle,
            variants = variants,
        )

    fun variant(
        id: String,
        conditions: List<NotificationTemplateCondition>,
        emoji: String?,
        title: String,
        body: String,
        compact: String,
        aggregateTitle: String,
    ): NotificationTemplateVariant =
        NotificationTemplateVariant(
            id = id,
            conditions = conditions,
            emoji = emoji,
            soloTitle = title,
            soloBody = body,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    fun condition(
        field: NotificationPackVariantField,
        value: String,
    ): NotificationTemplateCondition =
        NotificationTemplateCondition(
            field = field,
            value = value,
        )

    fun defaultPlayerOfflineTemplate(): NotificationTemplate =
        NotificationTemplate(
            emoji = "🌙",
            soloTitle = "Игрок вышел из игры",
            soloBody = "{player} больше не в сети.",
            compact = "{player} вышел из игры",
            aggregateTitle = "Игроки вышли из игры",
        )

    private fun normalizeItemDropVariants(
        key: String,
        baseEmoji: String?,
        variants: List<NotificationTemplateVariant>,
    ): List<NotificationTemplateVariant> {
        val explicitSecretTypes =
            variants
                .filter { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_RARITY &&
                            condition.value == "SECRET"
                    }
                }
                .mapNotNull { variant ->
                    variant.conditions
                        .firstOrNull { condition -> condition.field == NotificationPackVariantField.ITEM_TYPE }
                        ?.value
                }
                .toSet()

        return variants
            .flatMap { variant ->
                val rarity =
                    variant.conditions
                        .firstOrNull { condition -> condition.field == NotificationPackVariantField.ITEM_RARITY }
                        ?.value
                val itemType =
                    variant.conditions
                        .firstOrNull { condition -> condition.field == NotificationPackVariantField.ITEM_TYPE }
                        ?.value

                when {
                    // A generic MYTHICAL variant made the pack's base drop copy unreachable.
                    rarity == "MYTHICAL" && itemType == null -> emptyList()

                    // SECRET drops only exist as sword/pet variants. Expand legacy generic SECRET
                    // copy only for a type that does not already have an explicitly authored variant.
                    rarity == "SECRET" && itemType == null ->
                        listOf("SWORD", "PET")
                            .filterNot { type -> type in explicitSecretTypes }
                            .map { type ->
                                variant.copy(
                                    id = "${variant.id}-${type.lowercase()}",
                                    conditions =
                                        variant.conditions +
                                            condition(NotificationPackVariantField.ITEM_TYPE, type),
                                )
                            }

                    rarity == "SECRET" && itemType !in SECRET_ITEM_TYPES -> emptyList()
                    else -> listOf(variant)
                }
            }
            .map { variant ->
                if (key in BASE_DROP_EMOJI_PACKS) {
                    variant.copy(emoji = baseEmoji)
                } else {
                    variant
                }
            }
    }

    private fun withCometChaosValue(
        key: String,
        valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueForms>>,
    ): Map<NotificationPackValueKey, Map<String, NotificationValueForms>> {
        val eventValues = valueMappings[NotificationPackValueKey.GLOBAL_EVENT].orEmpty()
        val cometValue =
            if (key == PROTOCOL_PACK_KEY) {
                NotificationValueForms("COMET CHAOS")
            } else {
                NotificationValueForms("Кометный хаос")
            }

        return valueMappings +
            (NotificationPackValueKey.GLOBAL_EVENT to (eventValues + (COMET_CHAOS to cometValue)))
    }

    private val SECRET_ITEM_TYPES = setOf("SWORD", "PET")

    // Most packs keep their signature drop emoji across base/sword/pet copy.
    // A smaller set intentionally keeps authored subtype emoji where it adds to the pack's joke/theme.
    private val BASE_DROP_EMOJI_PACKS =
        setOf(
            "notification.mnemos-archive",
            "notification.protocol",
            "notification.night-shift",
            "notification.dep",
            "notification.deadpan",
            "notification.anomaly",
            "notification.frontier",
            "notification.suspicious-luck",
            "notification.critical",
            "notification.shbshnik",
            "notification.kaif",
            "notification.gospodin",
            "notification.lavash",
            "notification.gossip",
            "notification.commentator",
            "notification.deadline",
        )

    private const val COMET_CHAOS = "COMET_CHAOS"
    private const val PROTOCOL_PACK_KEY = "notification.protocol"
}

object RussianNotificationValues {
    val all: Map<NotificationPackValueKey, Map<String, NotificationValueForms>> =
        mapOf(
            NotificationPackValueKey.BOOSTER to
                mapOf(
                    "LUCK" to forms("Удача", "genitive" to "Удачи", "accusative" to "Удачу", "instrumental" to "Удачей"),
                    "MONEY" to forms("Доход", "genitive" to "Дохода", "accusative" to "Доход", "instrumental" to "Доходом"),
                    "DAMAGE" to forms("Урон", "genitive" to "Урона", "accusative" to "Урон", "instrumental" to "Уроном"),
                    "POWER" to forms("Сила", "genitive" to "Силы", "accusative" to "Силу", "instrumental" to "Силой"),
                ),
            NotificationPackValueKey.DUNGEON to
                mapOf(
                    "EASY" to forms("Лёгкое подземелье", "genitive" to "Лёгкого подземелья"),
                    "MEDIUM" to forms("Среднее подземелье", "genitive" to "Среднего подземелья"),
                    "HARD" to forms("Сложное подземелье", "genitive" to "Сложного подземелья"),
                    "EXPERT" to forms("Экспертное подземелье", "genitive" to "Экспертного подземелья"),
                ),
            NotificationPackValueKey.GLOBAL_EVENT to
                mapOf(
                    "DARKNESS" to forms("Тьма"),
                    "MOON" to forms("Кровавая луна"),
                    "ECLIPSE" to forms("Затмение"),
                    "EXPLOSION" to forms("Взрыв"),
                    "COMET_CHAOS" to forms("Кометный хаос"),
                ),
            NotificationPackValueKey.ITEM_RARITY to
                mapOf(
                    "MYTHICAL" to forms("Мифический"),
                    "SECRET" to forms("Секретный"),
                ),
            NotificationPackValueKey.ITEM_TYPE to
                mapOf(
                    "SWORD" to forms("меч", "genitive" to "меча", "accusative" to "меч"),
                    "AURA" to forms("аура", "genitive" to "ауры", "accusative" to "ауру"),
                    "PET" to forms("питомец", "genitive" to "питомца", "accusative" to "питомца"),
                    "BOOK" to forms("книга", "genitive" to "книги", "accusative" to "книгу"),
                    "JEWELRY" to forms("реликвия", "genitive" to "реликвии", "accusative" to "реликвию"),
                ),
        )

    private fun forms(
        default: String,
        vararg forms: Pair<String, String>,
    ): NotificationValueForms =
        NotificationValueForms(
            default = default,
            forms = forms.toMap(),
        )
}
