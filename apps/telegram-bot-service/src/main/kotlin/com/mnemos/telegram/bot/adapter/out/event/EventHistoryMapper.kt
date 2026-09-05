package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedItemResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage

object EventHistoryMapper {
    fun InternalEventFeedResponse.toModel(
        localization: Map<String, String> = emptyMap(),
    ): EventHistoryPage =
        EventHistoryPage(
            items = items.map { item -> item.toModel(localization) },
            cursor = cursor,
        )

    private fun InternalEventFeedItemResponse.toModel(localization: Map<String, String>): EventHistoryItem =
        when (type) {
            InternalEventFeedType.ITEM_DROP -> {
                val rarity = requireNotNull(itemRarity)
                val type = requireNotNull(itemType)

                EventHistoryItem.ItemDrop(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    itemId = requireNotNull(itemId),
                    itemName = localized(localization, itemKey) ?: requireNotNull(itemName),
                    itemRarity = localized(localization, rarityKey(rarity)) ?: rarity.name,
                    itemType = localized(localization, itemTypeKey(type)) ?: type,
                    droppedFor = requireNotNull(droppedFor),
                )
            }

            InternalEventFeedType.BOOSTER -> {
                val type = requireNotNull(boosterType)

                EventHistoryItem.Booster(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    boosterType = localized(localization, boosterKey(type)) ?: type.name,
                    activatedBy = requireNotNull(activatedBy),
                )
            }

            InternalEventFeedType.GLOBAL -> {
                val type = requireNotNull(globalType)

                EventHistoryItem.Global(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    globalType = localized(localization, globalNameKey(type)) ?: type.name,
                )
            }

            InternalEventFeedType.RAID ->
                EventHistoryItem.Raid(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    locations =
                        locations.map { location ->
                            localized(localization, "sao.location.$location.name") ?: location.toString()
                        },
                )
        }

    private fun localized(
        localization: Map<String, String>,
        key: String?,
    ): String? =
        key
            ?.takeIf(String::isNotBlank)
            ?.let(localization::get)
            ?.replace(STYLE_PREFIX, "")
            ?.replace(MINECRAFT_FORMATTING, "")
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun rarityKey(rarity: ContractItemRarity): String =
        when (rarity) {
            ContractItemRarity.MYTHICAL -> "sao.enum.rarity.mythic.name"
            ContractItemRarity.SECRET -> "sao.enum.rarity.secret.name"
        }

    private fun itemTypeKey(type: String): String =
        when (type) {
            "SWORD" -> "sao.enum.item_type.weapon.name"
            "PET" -> "sao.enum.item_type.pet.name"
            "AURA" -> "sao.enum.item_type.aura.name"
            "BOOK" -> "sao.enum.item_type.book_active.name"
            "JEWELRY" -> "sao.enum.item_type.relic.name"
            else -> ""
        }

    private fun boosterKey(type: ContractBoosterType): String =
        when (type) {
            ContractBoosterType.LUCK -> "sao.enum.boost.luck.name"
            ContractBoosterType.MONEY -> "sao.enum.boost.coin.name"
            ContractBoosterType.DAMAGE -> "sao.enum.boost.damage.name"
            ContractBoosterType.POWER -> "sao.enum.boost.power.name"
        }

    private fun globalNameKey(type: ContractGlobalEventType): String =
        when (type) {
            ContractGlobalEventType.DARKNESS -> "sao.event.darkness.name"
            ContractGlobalEventType.MOON -> "sao.event.blood_moon.name"
            ContractGlobalEventType.ECLIPSE -> "sao.event.solar_eclipse.name"
            ContractGlobalEventType.EXPLOSION -> "sao.event.sunburst.name"
            ContractGlobalEventType.COMET_CHAOS -> "sao.event.comet_chaos.name"
        }

    private val STYLE_PREFIX = Regex("^¨[0-9a-fA-F]{6}")
    private val MINECRAFT_FORMATTING = Regex("§[0-9a-fk-orA-FK-OR]")
}
