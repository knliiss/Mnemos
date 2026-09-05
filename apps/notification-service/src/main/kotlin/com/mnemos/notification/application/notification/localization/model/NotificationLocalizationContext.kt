package com.mnemos.notification.application.notification.localization.model

import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.GlobalNotificationPayload
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType

data class NotificationLocalizationContext(
    val language: LocalizationLanguage,
    private val saoProperties: Map<String, String>,
) {
    fun itemName(payload: ItemDropNotificationPayload): String =
        payload.itemKey
            ?.let(::gameValue)
            ?: payload.itemName

    fun itemTypeName(type: NotificationItemType): String = gameValue(itemTypeKey(type)) ?: localizedItemTypeFallback(type)

    fun itemRarityName(rarity: NotificationItemRarity): String = gameValue(itemRarityKey(rarity)) ?: localizedItemRarityFallback(rarity)

    fun boosterName(payload: BoosterNotificationPayload): String = boosterName(payload.boosterType)

    fun boosterName(type: DomainBoosterType): String = gameValue(boosterKey(type)) ?: localizedBoosterFallback(type)

    fun dungeonName(type: DomainDungeonType): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    DomainDungeonType.EASY -> "Easy dungeon"
                    DomainDungeonType.MEDIUM -> "Medium dungeon"
                    DomainDungeonType.HARD -> "Hard dungeon"
                    DomainDungeonType.EXPERT -> "Expert dungeon"
                }

            LocalizationLanguage.RU ->
                when (type) {
                    DomainDungeonType.EASY -> "Лёгкое подземелье"
                    DomainDungeonType.MEDIUM -> "Среднее подземелье"
                    DomainDungeonType.HARD -> "Сложное подземелье"
                    DomainDungeonType.EXPERT -> "Экспертное подземелье"
                }

            LocalizationLanguage.UK ->
                when (type) {
                    DomainDungeonType.EASY -> "Легке підземелля"
                    DomainDungeonType.MEDIUM -> "Середнє підземелля"
                    DomainDungeonType.HARD -> "Складне підземелля"
                    DomainDungeonType.EXPERT -> "Експертне підземелля"
                }
        }

    fun raidLocations(payload: RaidNotificationPayload): String =
        payload.locations
            .sorted()
            .joinToString(", ") { locationId ->
                gameValue("sao.location.$locationId.name") ?: locationId.toString()
            }

    fun globalAlert(payload: GlobalNotificationPayload): String =
        gameValue(globalAlertKey(payload.eventType)) ?: localizedGlobalFallback(payload.eventType)

    fun globalName(eventType: DomainGlobalEventType): String = localizedGlobalName(eventType)

    private fun gameValue(key: String): String? =
        saoProperties[key]
            ?.replace(STYLE_PREFIX, "")
            ?.replace(MINECRAFT_FORMATTING, "")
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun localizedItemTypeFallback(type: NotificationItemType): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    NotificationItemType.SWORD -> "weapon"
                    NotificationItemType.PET -> "pet"
                    NotificationItemType.AURA -> "aura"
                    NotificationItemType.BOOK -> "book"
                    NotificationItemType.JEWELRY -> "relic"
                }

            LocalizationLanguage.RU ->
                when (type) {
                    NotificationItemType.SWORD -> "оружие"
                    NotificationItemType.PET -> "питомец"
                    NotificationItemType.AURA -> "аура"
                    NotificationItemType.BOOK -> "книга"
                    NotificationItemType.JEWELRY -> "реликвия"
                }

            LocalizationLanguage.UK ->
                when (type) {
                    NotificationItemType.SWORD -> "зброя"
                    NotificationItemType.PET -> "улюбленець"
                    NotificationItemType.AURA -> "аура"
                    NotificationItemType.BOOK -> "книга"
                    NotificationItemType.JEWELRY -> "реліквія"
                }
        }

    private fun localizedItemRarityFallback(rarity: NotificationItemRarity): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (rarity) {
                    NotificationItemRarity.MYTHICAL -> "Mythic"
                    NotificationItemRarity.SECRET -> "Secret"
                }

            LocalizationLanguage.RU ->
                when (rarity) {
                    NotificationItemRarity.MYTHICAL -> "Мифический"
                    NotificationItemRarity.SECRET -> "Секретный"
                }

            LocalizationLanguage.UK ->
                when (rarity) {
                    NotificationItemRarity.MYTHICAL -> "Міфічний"
                    NotificationItemRarity.SECRET -> "Секретний"
                }
        }

    private fun localizedBoosterFallback(type: DomainBoosterType): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    DomainBoosterType.LUCK -> "Luck Booster"
                    DomainBoosterType.MONEY -> "Money Booster"
                    DomainBoosterType.DAMAGE -> "Damage Booster"
                    DomainBoosterType.POWER -> "Power Booster"
                }

            LocalizationLanguage.RU ->
                when (type) {
                    DomainBoosterType.LUCK -> "Бустер удачи"
                    DomainBoosterType.MONEY -> "Бустер денег"
                    DomainBoosterType.DAMAGE -> "Бустер урона"
                    DomainBoosterType.POWER -> "Бустер силы"
                }

            LocalizationLanguage.UK ->
                when (type) {
                    DomainBoosterType.LUCK -> "Бустер удачі"
                    DomainBoosterType.MONEY -> "Бустер грошей"
                    DomainBoosterType.DAMAGE -> "Бустер шкоди"
                    DomainBoosterType.POWER -> "Бустер сили"
                }
        }

    private fun localizedGlobalFallback(type: DomainGlobalEventType): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    DomainGlobalEventType.DARKNESS -> "Darkness has fallen."
                    DomainGlobalEventType.EXPLOSION -> "A solar burst has begun."
                    DomainGlobalEventType.MOON -> "The blood moon has risen."
                    DomainGlobalEventType.ECLIPSE -> "A solar eclipse has begun."
                    DomainGlobalEventType.COMET_CHAOS -> "Comet chaos has begun."
                }

            LocalizationLanguage.RU ->
                when (type) {
                    DomainGlobalEventType.DARKNESS -> "Наступила Тьма."
                    DomainGlobalEventType.EXPLOSION -> "Начался Солнечный взрыв."
                    DomainGlobalEventType.MOON -> "Взошла Кровавая луна."
                    DomainGlobalEventType.ECLIPSE -> "Началось Затмение."
                    DomainGlobalEventType.COMET_CHAOS -> "Начался Кометный хаос."
                }

            LocalizationLanguage.UK ->
                when (type) {
                    DomainGlobalEventType.DARKNESS -> "Настала Пітьма."
                    DomainGlobalEventType.EXPLOSION -> "Почався Сонячний вибух."
                    DomainGlobalEventType.MOON -> "Зійшов Кривавий місяць."
                    DomainGlobalEventType.ECLIPSE -> "Почалося Затемнення."
                    DomainGlobalEventType.COMET_CHAOS -> "Почався Кометний хаос."
                }
        }

    private fun localizedGlobalName(type: DomainGlobalEventType): String =
        when (language) {
            LocalizationLanguage.EN ->
                when (type) {
                    DomainGlobalEventType.DARKNESS -> "Darkness"
                    DomainGlobalEventType.EXPLOSION -> "Solar Burst"
                    DomainGlobalEventType.MOON -> "Blood Moon"
                    DomainGlobalEventType.ECLIPSE -> "Solar Eclipse"
                    DomainGlobalEventType.COMET_CHAOS -> "Comet Chaos"
                }

            LocalizationLanguage.RU ->
                when (type) {
                    DomainGlobalEventType.DARKNESS -> "Тьма"
                    DomainGlobalEventType.EXPLOSION -> "Солнечный взрыв"
                    DomainGlobalEventType.MOON -> "Кровавая луна"
                    DomainGlobalEventType.ECLIPSE -> "Затмение"
                    DomainGlobalEventType.COMET_CHAOS -> "Кометный хаос"
                }

            LocalizationLanguage.UK ->
                when (type) {
                    DomainGlobalEventType.DARKNESS -> "Пітьма"
                    DomainGlobalEventType.EXPLOSION -> "Сонячний вибух"
                    DomainGlobalEventType.MOON -> "Кривавий місяць"
                    DomainGlobalEventType.ECLIPSE -> "Затемнення"
                    DomainGlobalEventType.COMET_CHAOS -> "Кометний хаос"
                }
        }

    private companion object {
        val STYLE_PREFIX = Regex("^¨[0-9a-fA-F]{6}")
        val MINECRAFT_FORMATTING = Regex("§[0-9a-fk-orA-FK-OR]")

        fun itemTypeKey(type: NotificationItemType): String =
            when (type) {
                NotificationItemType.SWORD -> "sao.enum.item_type.weapon.name"
                NotificationItemType.PET -> "sao.enum.item_type.pet.name"
                NotificationItemType.AURA -> "sao.enum.item_type.aura.name"
                NotificationItemType.BOOK -> "sao.enum.item_type.book_active.name"
                NotificationItemType.JEWELRY -> "sao.enum.item_type.relic.name"
            }

        fun itemRarityKey(rarity: NotificationItemRarity): String =
            when (rarity) {
                NotificationItemRarity.MYTHICAL -> "sao.enum.rarity.mythic.name"
                NotificationItemRarity.SECRET -> "sao.enum.rarity.secret.name"
            }

        fun boosterKey(type: DomainBoosterType): String =
            when (type) {
                DomainBoosterType.LUCK -> "sao.enum.boost.luck.name"
                DomainBoosterType.MONEY -> "sao.enum.boost.coin.name"
                DomainBoosterType.DAMAGE -> "sao.enum.boost.damage.name"
                DomainBoosterType.POWER -> "sao.enum.boost.power.name"
            }

        fun globalAlertKey(type: DomainGlobalEventType): String =
            when (type) {
                DomainGlobalEventType.DARKNESS -> "sao.event.darkness.alert"
                DomainGlobalEventType.EXPLOSION -> "sao.event.sunburst.alert"
                DomainGlobalEventType.MOON -> "sao.event.blood_moon.alert"
                DomainGlobalEventType.ECLIPSE -> "sao.event.solar_eclipse.alert"
                DomainGlobalEventType.COMET_CHAOS -> "sao.event.comet_chaos.alert"
            }
    }
}
