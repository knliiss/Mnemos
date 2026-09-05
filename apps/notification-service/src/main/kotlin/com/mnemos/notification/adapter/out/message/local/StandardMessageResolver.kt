package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.notification.application.notification.localization.model.NotificationLocalizationContext
import com.mnemos.notification.application.notification.model.BoosterNotificationPayload
import com.mnemos.notification.application.notification.model.DungeonNotificationPayload
import com.mnemos.notification.application.notification.model.GlobalNotificationPayload
import com.mnemos.notification.application.notification.model.ItemDropNotificationPayload
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationItemRarity
import com.mnemos.notification.application.notification.model.NotificationItemType
import com.mnemos.notification.application.notification.model.PlayerOfflineNotificationPayload
import com.mnemos.notification.application.notification.model.RaidNotificationPayload
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import org.springframework.stereotype.Component

@Component
class StandardMessageResolver {
    fun resolve(item: NotificationItem): ResolvedNotification = resolve(item, DEFAULT_LOCALIZATION)

    fun resolve(
        item: NotificationItem,
        localization: NotificationLocalizationContext,
    ): ResolvedNotification =
        when (val payload = item.payload) {
            is ItemDropNotificationPayload -> resolve(payload, localization)
            is BoosterNotificationPayload -> resolve(payload, localization)
            is DungeonNotificationPayload -> resolve(payload, localization.language)
            is RaidNotificationPayload -> resolve(payload, localization)
            is GlobalNotificationPayload -> resolve(payload, localization)
            is PlayerOfflineNotificationPayload -> resolve(payload, localization.language)
        }

    private fun resolve(
        payload: ItemDropNotificationPayload,
        localization: NotificationLocalizationContext,
    ): ResolvedNotification {
        val presentation = resolveItem(payload.itemType, payload.itemRarity, localization.language)
        val player = payload.droppedFor.escapeHtml()
        val itemName = localization.itemName(payload).escapeHtml()

        val body =
            when (localization.language) {
                LocalizationLanguage.EN -> "Player <b>$player</b> dropped <b>“$itemName”</b>."
                LocalizationLanguage.RU -> "Игрок <b>$player</b> выбил ${presentation.accusative} <b>«$itemName»</b>."
                LocalizationLanguage.UK -> "Гравець <b>$player</b> вибив ${presentation.accusative} <b>«$itemName»</b>."
            }

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = body,
            compact = "${presentation.emoji} <b>$player</b> — ${presentation.compact} <b>«$itemName»</b>",
        )
    }

    private fun resolve(
        payload: BoosterNotificationPayload,
        localization: NotificationLocalizationContext,
    ): ResolvedNotification {
        val presentation = resolveBooster(payload.boosterType, localization.language)
        val player = payload.activatedBy.escapeHtml()
        val boosterName = localization.boosterName(payload).escapeHtml()

        val body =
            when (localization.language) {
                LocalizationLanguage.EN -> "Player <b>$player</b> activated <b>$boosterName</b>."
                LocalizationLanguage.RU -> "Игрок <b>$player</b> активировал <b>$boosterName</b>."
                LocalizationLanguage.UK -> "Гравець <b>$player</b> активував <b>$boosterName</b>."
            }

        val compact =
            when (localization.language) {
                LocalizationLanguage.EN -> "${presentation.emoji} <b>$boosterName</b> — activated by <b>$player</b>"
                LocalizationLanguage.RU -> "${presentation.emoji} <b>$boosterName</b> — активировал <b>$player</b>"
                LocalizationLanguage.UK -> "${presentation.emoji} <b>$boosterName</b> — активував <b>$player</b>"
            }

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = body,
            compact = compact,
        )
    }

    private fun resolve(
        payload: DungeonNotificationPayload,
        language: LocalizationLanguage,
    ): ResolvedNotification {
        val presentation = resolveDungeon(payload.dungeonType, language)

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = presentation.body,
            compact = "${presentation.emoji} <b>${presentation.compact}</b> ${presentation.compactSuffix}",
        )
    }

    private fun resolve(
        payload: RaidNotificationPayload,
        localization: NotificationLocalizationContext,
    ): ResolvedNotification {
        val locations = localization.raidLocations(payload).escapeHtml()
        val multiple = payload.locations.size > 1

        return when (localization.language) {
            LocalizationLanguage.EN ->
                ResolvedNotification(
                    type = payload.type,
                    soloTitle = if (multiple) "🛡️ <b>Raids started</b>" else "⚔️ <b>Raid started</b>",
                    soloBody =
                        if (multiple) {
                            "Raids appeared at <b>$locations</b>."
                        } else {
                            "A raid appeared at <b>$locations</b>."
                        },
                    compact = if (multiple) "🛡️ Raids — <b>$locations</b>" else "⚔️ Raid — <b>$locations</b>",
                )

            LocalizationLanguage.RU ->
                ResolvedNotification(
                    type = payload.type,
                    soloTitle = if (multiple) "🛡️ <b>Начались рейды</b>" else "⚔️ <b>Начался рейд</b>",
                    soloBody =
                        if (multiple) {
                            "Рейды появились на локациях <b>$locations</b>."
                        } else {
                            "Рейд появился на локации <b>$locations</b>."
                        },
                    compact = if (multiple) "🛡️ Рейды — <b>$locations</b>" else "⚔️ Рейд — <b>$locations</b>",
                )

            LocalizationLanguage.UK ->
                ResolvedNotification(
                    type = payload.type,
                    soloTitle = if (multiple) "🛡️ <b>Почалися рейди</b>" else "⚔️ <b>Почався рейд</b>",
                    soloBody =
                        if (multiple) {
                            "Рейди з'явилися на локаціях <b>$locations</b>."
                        } else {
                            "Рейд з'явився на локації <b>$locations</b>."
                        },
                    compact = if (multiple) "🛡️ Рейди — <b>$locations</b>" else "⚔️ Рейд — <b>$locations</b>",
                )
        }
    }

    private fun resolve(
        payload: GlobalNotificationPayload,
        localization: NotificationLocalizationContext,
    ): ResolvedNotification {
        val presentation = resolveGlobal(payload.eventType, localization.language)
        val alert = localization.globalAlert(payload).escapeHtml()

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = alert,
            compact = "${presentation.emoji} <b>${presentation.compact}</b>",
        )
    }

    private fun resolve(
        payload: PlayerOfflineNotificationPayload,
        language: LocalizationLanguage,
    ): ResolvedNotification {
        val player = payload.player.escapeHtml()
        val realm = payload.previousRealm?.escapeHtml()

        return when (language) {
            LocalizationLanguage.EN ->
                ResolvedNotification(
                    type = payload.type,
                    soloTitle = "🌙 <b>Player went offline</b>",
                    soloBody =
                        if (realm == null) {
                            "<b>$player</b> is no longer online."
                        } else {
                            "<b>$player</b> is no longer online. Last realm: <b>$realm</b>."
                        },
                    compact = "🌙 <b>$player</b> went offline",
                )

            LocalizationLanguage.RU ->
                ResolvedNotification(
                    type = payload.type,
                    soloTitle = "🌙 <b>Игрок вышел из игры</b>",
                    soloBody =
                        if (realm == null) {
                            "<b>$player</b> больше не в сети."
                        } else {
                            "<b>$player</b> больше не в сети. Последний реалм — <b>$realm</b>."
                        },
                    compact = "🌙 <b>$player</b> вышел из игры",
                )

            LocalizationLanguage.UK ->
                ResolvedNotification(
                    type = payload.type,
                    soloTitle = "🌙 <b>Гравець вийшов із гри</b>",
                    soloBody =
                        if (realm == null) {
                            "<b>$player</b> більше не в мережі."
                        } else {
                            "<b>$player</b> більше не в мережі. Останній реалм — <b>$realm</b>."
                        },
                    compact = "🌙 <b>$player</b> вийшов із гри",
                )
        }
    }

    private fun resolveItem(
        type: NotificationItemType,
        rarity: NotificationItemRarity,
        language: LocalizationLanguage,
    ): ItemPresentation {
        val emoji = itemEmoji(type, rarity)

        return when (language) {
            LocalizationLanguage.EN ->
                ItemPresentation(
                    emoji = emoji,
                    title = "${rarity.englishName()} ${type.englishName()} drop",
                    accusative = "",
                    compact = "${rarity.englishName().lowercase()} ${type.englishName().lowercase()}",
                )

            LocalizationLanguage.RU -> resolveRussianItem(type, rarity, emoji)
            LocalizationLanguage.UK -> resolveUkrainianItem(type, rarity, emoji)
        }
    }

    private fun resolveRussianItem(
        type: NotificationItemType,
        rarity: NotificationItemRarity,
        emoji: String,
    ): ItemPresentation =
        when (rarity to type) {
            NotificationItemRarity.SECRET to NotificationItemType.SWORD ->
                ItemPresentation(emoji, "Новое секретное оружие", "секретное оружие", "секретное оружие")

            NotificationItemRarity.MYTHICAL to NotificationItemType.SWORD ->
                ItemPresentation(emoji, "Новое мифическое оружие", "мифическое оружие", "мифическое оружие")

            NotificationItemRarity.SECRET to NotificationItemType.PET ->
                ItemPresentation(emoji, "Новый секретный питомец", "секретного питомца", "секретный питомец")

            NotificationItemRarity.MYTHICAL to NotificationItemType.PET ->
                ItemPresentation(emoji, "Новый мифический питомец", "мифического питомца", "мифический питомец")

            NotificationItemRarity.SECRET to NotificationItemType.AURA ->
                ItemPresentation(emoji, "Новая секретная аура", "секретную ауру", "секретная аура")

            NotificationItemRarity.MYTHICAL to NotificationItemType.AURA ->
                ItemPresentation(emoji, "Новая мифическая аура", "мифическую ауру", "мифическая аура")

            NotificationItemRarity.SECRET to NotificationItemType.BOOK ->
                ItemPresentation(emoji, "Новая секретная книга", "секретную книгу", "секретная книга")

            NotificationItemRarity.MYTHICAL to NotificationItemType.BOOK ->
                ItemPresentation(emoji, "Новая мифическая книга", "мифическую книгу", "мифическая книга")

            NotificationItemRarity.SECRET to NotificationItemType.JEWELRY ->
                ItemPresentation(emoji, "Новое секретное украшение", "секретное украшение", "секретное украшение")

            NotificationItemRarity.MYTHICAL to NotificationItemType.JEWELRY ->
                ItemPresentation(emoji, "Новое мифическое украшение", "мифическое украшение", "мифическое украшение")

            else -> unsupportedItemPresentation(type, rarity)
        }

    private fun resolveUkrainianItem(
        type: NotificationItemType,
        rarity: NotificationItemRarity,
        emoji: String,
    ): ItemPresentation =
        when (rarity to type) {
            NotificationItemRarity.SECRET to NotificationItemType.SWORD ->
                ItemPresentation(emoji, "Нова секретна зброя", "секретну зброю", "секретна зброя")

            NotificationItemRarity.MYTHICAL to NotificationItemType.SWORD ->
                ItemPresentation(emoji, "Нова міфічна зброя", "міфічну зброю", "міфічна зброя")

            NotificationItemRarity.SECRET to NotificationItemType.PET ->
                ItemPresentation(emoji, "Новий секретний улюбленець", "секретного улюбленця", "секретний улюбленець")

            NotificationItemRarity.MYTHICAL to NotificationItemType.PET ->
                ItemPresentation(emoji, "Новий міфічний улюбленець", "міфічного улюбленця", "міфічний улюбленець")

            NotificationItemRarity.SECRET to NotificationItemType.AURA ->
                ItemPresentation(emoji, "Нова секретна аура", "секретну ауру", "секретна аура")

            NotificationItemRarity.MYTHICAL to NotificationItemType.AURA ->
                ItemPresentation(emoji, "Нова міфічна аура", "міфічну ауру", "міфічна аура")

            NotificationItemRarity.SECRET to NotificationItemType.BOOK ->
                ItemPresentation(emoji, "Нова секретна книга", "секретну книгу", "секретна книга")

            NotificationItemRarity.MYTHICAL to NotificationItemType.BOOK ->
                ItemPresentation(emoji, "Нова міфічна книга", "міфічну книгу", "міфічна книга")

            NotificationItemRarity.SECRET to NotificationItemType.JEWELRY ->
                ItemPresentation(emoji, "Нова секретна реліквія", "секретну реліквію", "секретна реліквія")

            NotificationItemRarity.MYTHICAL to NotificationItemType.JEWELRY ->
                ItemPresentation(emoji, "Нова міфічна реліквія", "міфічну реліквію", "міфічна реліквія")

            else -> unsupportedItemPresentation(type, rarity)
        }

    private fun itemEmoji(
        type: NotificationItemType,
        rarity: NotificationItemRarity,
    ): String =
        when (rarity to type) {
            NotificationItemRarity.SECRET to NotificationItemType.SWORD -> "🗡️"
            NotificationItemRarity.MYTHICAL to NotificationItemType.SWORD -> "⚔️"
            NotificationItemRarity.SECRET to NotificationItemType.PET -> "🐾"
            NotificationItemRarity.MYTHICAL to NotificationItemType.PET -> "🐲"
            NotificationItemRarity.SECRET to NotificationItemType.AURA -> "✨"
            NotificationItemRarity.MYTHICAL to NotificationItemType.AURA -> "🌌"
            NotificationItemRarity.SECRET to NotificationItemType.BOOK -> "📕"
            NotificationItemRarity.MYTHICAL to NotificationItemType.BOOK -> "📜"
            NotificationItemRarity.SECRET to NotificationItemType.JEWELRY -> "💍"
            NotificationItemRarity.MYTHICAL to NotificationItemType.JEWELRY -> "💎"
            else -> unsupportedItemPresentation(type, rarity)
        }

    private fun unsupportedItemPresentation(
        type: NotificationItemType,
        rarity: NotificationItemRarity,
    ): Nothing = error("Unsupported item presentation: type=$type, rarity=$rarity")

    private fun resolveBooster(
        type: DomainBoosterType,
        language: LocalizationLanguage,
    ): BoosterPresentation {
        val emoji =
            when (type) {
                DomainBoosterType.LUCK -> "🍀"
                DomainBoosterType.MONEY -> "💰"
                DomainBoosterType.DAMAGE -> "⚔️"
                DomainBoosterType.POWER -> "🔥"
            }

        val title =
            when (language) {
                LocalizationLanguage.EN -> "${type.englishName()} activated"
                LocalizationLanguage.RU ->
                    when (type) {
                        DomainBoosterType.LUCK -> "Бустер удачи активирован"
                        DomainBoosterType.MONEY -> "Денежный бустер активирован"
                        DomainBoosterType.DAMAGE -> "Бустер урона активирован"
                        DomainBoosterType.POWER -> "Бустер силы активирован"
                    }

                LocalizationLanguage.UK ->
                    when (type) {
                        DomainBoosterType.LUCK -> "Бустер удачі активовано"
                        DomainBoosterType.MONEY -> "Бустер грошей активовано"
                        DomainBoosterType.DAMAGE -> "Бустер шкоди активовано"
                        DomainBoosterType.POWER -> "Бустер сили активовано"
                    }
            }

        return BoosterPresentation(
            emoji = emoji,
            title = title,
        )
    }

    private fun resolveDungeon(
        type: DomainDungeonType,
        language: LocalizationLanguage,
    ): DungeonPresentation =
        when (language) {
            LocalizationLanguage.EN -> resolveEnglishDungeon(type)
            LocalizationLanguage.RU -> resolveRussianDungeon(type)
            LocalizationLanguage.UK -> resolveUkrainianDungeon(type)
        }

    private fun resolveEnglishDungeon(type: DomainDungeonType): DungeonPresentation =
        when (type) {
            DomainDungeonType.EASY ->
                DungeonPresentation(
                    emoji = "🌿",
                    title = "Easy dungeon opened",
                    compact = "Easy dungeon",
                    compactSuffix = "is open",
                    body = "Time for a quick loot run.",
                )

            DomainDungeonType.MEDIUM ->
                DungeonPresentation(
                    emoji = "🏰",
                    title = "Medium dungeon opened",
                    compact = "Medium dungeon",
                    compactSuffix = "is open",
                    body = "The medium dungeon is now available.",
                )

            DomainDungeonType.HARD ->
                DungeonPresentation(
                    emoji = "🔥",
                    title = "Hard dungeon opened",
                    compact = "Hard dungeon",
                    compactSuffix = "is open",
                    body = "Get ready — this one will not be easy.",
                )

            DomainDungeonType.EXPERT ->
                DungeonPresentation(
                    emoji = "💀",
                    title = "Expert dungeon opened",
                    compact = "Expert dungeon",
                    compactSuffix = "is open",
                    body = "For players ready for the toughest challenge.",
                )
        }

    private fun resolveRussianDungeon(type: DomainDungeonType): DungeonPresentation =
        when (type) {
            DomainDungeonType.EASY ->
                DungeonPresentation(
                    emoji = "🌿",
                    title = "Открылось лёгкое подземелье",
                    compact = "Лёгкое подземелье",
                    compactSuffix = "уже открыто",
                    body = "Самое время отправиться за быстрой добычей.",
                )

            DomainDungeonType.MEDIUM ->
                DungeonPresentation(
                    emoji = "🏰",
                    title = "Открылось среднее подземелье",
                    compact = "Среднее подземелье",
                    compactSuffix = "уже открыто",
                    body = "Подземелье средней сложности уже доступно.",
                )

            DomainDungeonType.HARD ->
                DungeonPresentation(
                    emoji = "🔥",
                    title = "Открылось сложное подземелье",
                    compact = "Сложное подземелье",
                    compactSuffix = "уже открыто",
                    body = "Приготовьтесь — лёгкой прогулки здесь не будет.",
                )

            DomainDungeonType.EXPERT ->
                DungeonPresentation(
                    emoji = "💀",
                    title = "Открылось экспертное подземелье",
                    compact = "Экспертное подземелье",
                    compactSuffix = "уже открыто",
                    body = "Для тех, кто готов к самому серьёзному испытанию.",
                )
        }

    private fun resolveUkrainianDungeon(type: DomainDungeonType): DungeonPresentation =
        when (type) {
            DomainDungeonType.EASY ->
                DungeonPresentation(
                    emoji = "🌿",
                    title = "Відкрилося легке підземелля",
                    compact = "Легке підземелля",
                    compactSuffix = "вже відкрите",
                    body = "Саме час вирушити за швидкою здобиччю.",
                )

            DomainDungeonType.MEDIUM ->
                DungeonPresentation(
                    emoji = "🏰",
                    title = "Відкрилося середнє підземелля",
                    compact = "Середнє підземелля",
                    compactSuffix = "вже відкрите",
                    body = "Підземелля середньої складності вже доступне.",
                )

            DomainDungeonType.HARD ->
                DungeonPresentation(
                    emoji = "🔥",
                    title = "Відкрилося складне підземелля",
                    compact = "Складне підземелля",
                    compactSuffix = "вже відкрите",
                    body = "Приготуйтеся — легкої прогулянки тут не буде.",
                )

            DomainDungeonType.EXPERT ->
                DungeonPresentation(
                    emoji = "💀",
                    title = "Відкрилося експертне підземелля",
                    compact = "Експертне підземелля",
                    compactSuffix = "вже відкрите",
                    body = "Для тих, хто готовий до найсерйознішого випробування.",
                )
        }

    private fun resolveGlobal(
        type: DomainGlobalEventType,
        language: LocalizationLanguage,
    ): WorldEventPresentation {
        val emoji =
            when (type) {
                DomainGlobalEventType.DARKNESS -> "🌑"
                DomainGlobalEventType.EXPLOSION -> "☀️"
                DomainGlobalEventType.MOON -> "\uD83E\uDE78"
                DomainGlobalEventType.ECLIPSE -> "🌘"
                DomainGlobalEventType.COMET_CHAOS -> "☄️"
            }

        val name =
            NotificationLocalizationContext(
                language = language,
                saoProperties = emptyMap(),
            ).globalName(type)

        val title =
            when (language) {
                LocalizationLanguage.EN -> "$name started"
                LocalizationLanguage.RU -> "Событие: $name"
                LocalizationLanguage.UK -> "Подія: $name"
            }

        return WorldEventPresentation(
            emoji = emoji,
            title = title,
            compact = name,
        )
    }

    private fun NotificationItemRarity.englishName(): String =
        when (this) {
            NotificationItemRarity.MYTHICAL -> "Mythic"
            NotificationItemRarity.SECRET -> "Secret"
        }

    private fun NotificationItemType.englishName(): String =
        when (this) {
            NotificationItemType.SWORD -> "Weapon"
            NotificationItemType.PET -> "Pet"
            NotificationItemType.AURA -> "Aura"
            NotificationItemType.BOOK -> "Book"
            NotificationItemType.JEWELRY -> "Relic"
        }

    private fun DomainBoosterType.englishName(): String =
        when (this) {
            DomainBoosterType.LUCK -> "Luck Booster"
            DomainBoosterType.MONEY -> "Money Booster"
            DomainBoosterType.DAMAGE -> "Damage Booster"
            DomainBoosterType.POWER -> "Power Booster"
        }

    private fun String.escapeHtml(): String =
        replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private data class ItemPresentation(
        val emoji: String,
        val title: String,
        val accusative: String,
        val compact: String,
    )

    private data class BoosterPresentation(
        val emoji: String,
        val title: String,
    )

    private data class DungeonPresentation(
        val emoji: String,
        val title: String,
        val compact: String,
        val compactSuffix: String,
        val body: String,
    )

    private data class WorldEventPresentation(
        val emoji: String,
        val title: String,
        val compact: String,
    )

    private companion object {
        val DEFAULT_LOCALIZATION =
            NotificationLocalizationContext(
                language = LocalizationLanguage.RU,
                saoProperties = emptyMap(),
            )
    }
}
