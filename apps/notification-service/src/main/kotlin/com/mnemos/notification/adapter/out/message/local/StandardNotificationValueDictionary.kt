package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.localization.model.NotificationLocalizationContext
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import com.mnemos.notification.application.notification.personalization.model.NotificationPackValueForms
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey

object StandardNotificationValueDictionary {
    fun resolve(
        key: NotificationPackValueKey,
        rawValue: String,
        form: String?,
    ): String? = values[key]?.get(rawValue)?.resolve(form)

    fun resolve(
        key: NotificationPackValueKey,
        rawValue: String,
        form: String?,
        localization: NotificationLocalizationContext,
    ): String? {
        val localizedDefault = localizedDefault(key, rawValue, localization) ?: return null

        if (form == null || form == "default") {
            return localizedDefault
        }

        if (localization.language != LocalizationLanguage.RU) {
            return localizedDefault
        }

        return values[key]?.get(rawValue)?.resolve(form) ?: localizedDefault
    }

    private fun localizedDefault(
        key: NotificationPackValueKey,
        rawValue: String,
        localization: NotificationLocalizationContext,
    ): String? =
        when (key) {
            NotificationPackValueKey.BOOSTER ->
                enumValueOrNull<DomainBoosterType>(rawValue)?.let(localization::boosterName)

            NotificationPackValueKey.DUNGEON ->
                enumValueOrNull<DomainDungeonType>(rawValue)?.let(localization::dungeonName)

            NotificationPackValueKey.GLOBAL_EVENT ->
                enumValueOrNull<DomainGlobalEventType>(rawValue)?.let(localization::globalName)

            NotificationPackValueKey.ITEM_RARITY ->
                enumValueOrNull<NotificationItemRarity>(rawValue)?.let(localization::itemRarityName)

            NotificationPackValueKey.ITEM_TYPE ->
                enumValueOrNull<NotificationItemType>(rawValue)?.let(localization::itemTypeName)
        }

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
        enumValues<T>().firstOrNull { entry -> entry.name == value }

    private val values: Map<NotificationPackValueKey, Map<String, NotificationPackValueForms>> =
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
        vararg named: Pair<String, String>,
    ): NotificationPackValueForms =
        NotificationPackValueForms(
            default = default,
            forms = named.toMap(),
        )
}
