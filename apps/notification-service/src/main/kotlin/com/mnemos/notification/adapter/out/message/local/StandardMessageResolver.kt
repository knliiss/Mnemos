package com.mnemos.notification.adapter.out.message.local

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
    fun resolve(item: NotificationItem): ResolvedNotification =
        when (val payload = item.payload) {
            is ItemDropNotificationPayload -> resolve(payload)
            is BoosterNotificationPayload -> resolve(payload)
            is DungeonNotificationPayload -> resolve(payload)
            is RaidNotificationPayload -> resolve(payload)
            is GlobalNotificationPayload -> resolve(payload)
            is PlayerOfflineNotificationPayload -> resolve(payload)
        }

    private fun resolve(payload: ItemDropNotificationPayload): ResolvedNotification {
        val presentation = resolveItem(payload.itemType, payload.itemRarity)
        val player = payload.droppedFor.escapeHtml()
        val itemName = payload.itemName.escapeHtml()

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = "Игрок <b>$player</b> выбил ${presentation.accusative} <b>«$itemName»</b>.",
            compact = "${presentation.emoji} <b>$player</b> — ${presentation.compact} <b>«$itemName»</b>",
        )
    }

    private fun resolve(payload: BoosterNotificationPayload): ResolvedNotification {
        val presentation =
            when (payload.boosterType) {
                DomainBoosterType.LUCK ->
                    BoosterPresentation(
                        emoji = "🍀",
                        title = "Бустер удачи активирован",
                        name = "бустер удачи",
                        compact = "Удача",
                    )

                DomainBoosterType.MONEY ->
                    BoosterPresentation(
                        emoji = "💰",
                        title = "Денежный бустер активирован",
                        name = "денежный бустер",
                        compact = "Монеты",
                    )

                DomainBoosterType.DAMAGE ->
                    BoosterPresentation(
                        emoji = "⚔️",
                        title = "Бустер урона активирован",
                        name = "бустер урона",
                        compact = "Урон",
                    )

                DomainBoosterType.POWER ->
                    BoosterPresentation(
                        emoji = "🔥",
                        title = "Бустер силы активирован",
                        name = "бустер силы",
                        compact = "Сила",
                    )
            }

        val player = payload.activatedBy.escapeHtml()

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = "Игрок <b>$player</b> активировал ${presentation.name}.",
            compact = "${presentation.emoji} <b>${presentation.compact}</b> — активировал <b>$player</b>",
        )
    }

    private fun resolve(payload: DungeonNotificationPayload): ResolvedNotification {
        val presentation =
            when (payload.dungeonType) {
                DomainDungeonType.EASY ->
                    DungeonPresentation(
                        emoji = "🌿",
                        title = "Открылось лёгкое подземелье",
                        compact = "Лёгкое подземелье",
                        body = "Самое время отправиться за быстрой добычей.",
                    )

                DomainDungeonType.MEDIUM ->
                    DungeonPresentation(
                        emoji = "🏰",
                        title = "Открылось среднее подземелье",
                        compact = "Среднее подземелье",
                        body = "Подземелье средней сложности уже доступно.",
                    )

                DomainDungeonType.HARD ->
                    DungeonPresentation(
                        emoji = "🔥",
                        title = "Открылось сложное подземелье",
                        compact = "Сложное подземелье",
                        body = "Приготовьтесь — лёгкой прогулки здесь не будет.",
                    )

                DomainDungeonType.EXPERT ->
                    DungeonPresentation(
                        emoji = "💀",
                        title = "Открылось экспертное подземелье",
                        compact = "Экспертное подземелье",
                        body = "Для тех, кто готов к самому серьёзному испытанию.",
                    )
            }

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = presentation.body,
            compact = "${presentation.emoji} <b>${presentation.compact}</b> уже открыто",
        )
    }

    private fun resolve(payload: RaidNotificationPayload): ResolvedNotification {
        val locations = payload.locations.sorted().joinToString(", ")

        return if (payload.locations.size == 1) {
            ResolvedNotification(
                type = payload.type,
                soloTitle = "⚔️ <b>Начался рейд</b>",
                soloBody = "Рейд появился на локации <b>$locations</b>.",
                compact = "⚔️ Рейд — локация <b>$locations</b>",
            )
        } else {
            ResolvedNotification(
                type = payload.type,
                soloTitle = "🛡️ <b>Начались рейды</b>",
                soloBody = "Рейды появились на локациях <b>$locations</b>.",
                compact = "🛡️ Рейды — локации <b>$locations</b>",
            )
        }
    }

    private fun resolve(payload: GlobalNotificationPayload): ResolvedNotification {
        val presentation =
            when (payload.eventType) {
                DomainGlobalEventType.DARKNESS ->
                    WorldEventPresentation(
                        emoji = "🌑",
                        title = "Мир погрузился во Тьму",
                        body = "Свет угас. Наступила <b>Тьма</b>.",
                        compact = "Мир погрузился во <b>Тьму</b>",
                    )

                DomainGlobalEventType.EXPLOSION ->
                    WorldEventPresentation(
                        emoji = "☀️",
                        title = "Произошёл Солнечный взрыв",
                        body = "Мир озарила вспышка — начался <b>Солнечный взрыв</b>.",
                        compact = "Начался <b>Солнечный взрыв</b>",
                    )

                DomainGlobalEventType.MOON ->
                    WorldEventPresentation(
                        emoji = "\uD83E\uDE78",
                        title = "Взошла Кровавая луна",
                        body = "Небо окрасилось в багровый цвет. Началась <b>Кровавая луна</b>.",
                        compact = "Взошла <b>Кровавая луна</b>",
                    )

                DomainGlobalEventType.ECLIPSE ->
                    WorldEventPresentation(
                        emoji = "🌘",
                        title = "Началось Затмение",
                        body = "Свет померк — мир накрыло <b>Затмение</b>.",
                        compact = "Началось <b>Затмение</b>",
                    )

                DomainGlobalEventType.COMET_CHAOS ->
                    WorldEventPresentation(
                        emoji = "☄️",
                        title = "Начался Кометный хаос",
                        body = "Комета пронеслась по небу — начался <b>Кометный хаос</b>.",
                        compact = "Начался <b>Кометный хаос</b>",
                    )
            }

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "${presentation.emoji} <b>${presentation.title}</b>",
            soloBody = presentation.body,
            compact = "${presentation.emoji} ${presentation.compact}",
        )
    }

    private fun resolve(payload: PlayerOfflineNotificationPayload): ResolvedNotification {
        val player = payload.player.escapeHtml()
        val realmSuffix =
            payload.previousRealm
                ?.escapeHtml()
                ?.let { realm -> " Последний реалм — <b>$realm</b>." }
                .orEmpty()

        return ResolvedNotification(
            type = payload.type,
            soloTitle = "🌙 <b>Игрок вышел из игры</b>",
            soloBody = "<b>$player</b> больше не в сети.$realmSuffix",
            compact = "🌙 <b>$player</b> вышел из игры",
        )
    }

    private fun resolveItem(
        type: NotificationItemType,
        rarity: NotificationItemRarity,
    ): ItemPresentation =
        when (rarity to type) {
            NotificationItemRarity.SECRET to NotificationItemType.SWORD ->
                ItemPresentation(
                    emoji = "🗡️",
                    title = "Новое секретное оружие",
                    accusative = "секретное оружие",
                    compact = "секретное оружие",
                )

            NotificationItemRarity.MYTHICAL to NotificationItemType.SWORD ->
                ItemPresentation(
                    emoji = "⚔️",
                    title = "Новое мифическое оружие",
                    accusative = "мифическое оружие",
                    compact = "мифическое оружие",
                )

            NotificationItemRarity.SECRET to NotificationItemType.PET ->
                ItemPresentation(
                    emoji = "🐾",
                    title = "Новый секретный питомец",
                    accusative = "секретного питомца",
                    compact = "секретный питомец",
                )

            NotificationItemRarity.MYTHICAL to NotificationItemType.PET ->
                ItemPresentation(
                    emoji = "🐲",
                    title = "Новый мифический питомец",
                    accusative = "мифического питомца",
                    compact = "мифический питомец",
                )

            NotificationItemRarity.SECRET to NotificationItemType.AURA ->
                ItemPresentation(
                    emoji = "✨",
                    title = "Новая секретная аура",
                    accusative = "секретную ауру",
                    compact = "секретная аура",
                )

            NotificationItemRarity.MYTHICAL to NotificationItemType.AURA ->
                ItemPresentation(
                    emoji = "🌌",
                    title = "Новая мифическая аура",
                    accusative = "мифическую ауру",
                    compact = "мифическая аура",
                )

            NotificationItemRarity.SECRET to NotificationItemType.BOOK ->
                ItemPresentation(
                    emoji = "📕",
                    title = "Новая секретная книга",
                    accusative = "секретную книгу",
                    compact = "секретная книга",
                )

            NotificationItemRarity.MYTHICAL to NotificationItemType.BOOK ->
                ItemPresentation(
                    emoji = "📜",
                    title = "Новая мифическая книга",
                    accusative = "мифическую книгу",
                    compact = "мифическая книга",
                )

            NotificationItemRarity.SECRET to NotificationItemType.JEWELRY ->
                ItemPresentation(
                    emoji = "💍",
                    title = "Новое секретное украшение",
                    accusative = "секретное украшение",
                    compact = "секретное украшение",
                )

            NotificationItemRarity.MYTHICAL to NotificationItemType.JEWELRY ->
                ItemPresentation(
                    emoji = "💎",
                    title = "Новое мифическое украшение",
                    accusative = "мифическое украшение",
                    compact = "мифическое украшение",
                )

            else -> throw IllegalArgumentException("Unknown notification item: $type")
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
        val name: String,
        val compact: String,
    )

    private data class DungeonPresentation(
        val emoji: String,
        val title: String,
        val compact: String,
        val body: String,
    )

    private data class WorldEventPresentation(
        val emoji: String,
        val title: String,
        val body: String,
        val compact: String,
    )
}
