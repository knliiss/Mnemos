package com.mnemos.telegram.bot.application.events.model

import java.time.Instant
import java.util.UUID

enum class EventHistoryFilter {
    ALL,
    ITEM_DROP,
    BOOSTER,
    GLOBAL,
    RAID,
}

enum class EventHistorySort {
    NEWEST,
    OLDEST,
}

enum class EventHistoryItemRarity {
    MYTHICAL,
    SECRET,
}

enum class EventHistoryItemType {
    SWORD,
    AURA,
    PET,
    BOOK,
    JEWELRY,
}

enum class EventHistoryBoosterType {
    LUCK,
    MONEY,
    DAMAGE,
    POWER,
}

enum class EventHistoryGlobalType {
    DARKNESS,
    MOON,
    ECLIPSE,
    EXPLOSION,
    COMET_CHAOS,
}

data class EventHistoryQuery(
    val filter: EventHistoryFilter,
    val after: UUID?,
    val search: String? = null,
    val sort: EventHistorySort = EventHistorySort.NEWEST,
    val itemRarity: EventHistoryItemRarity? = null,
    val itemType: EventHistoryItemType? = null,
    val boosterType: EventHistoryBoosterType? = null,
    val globalType: EventHistoryGlobalType? = null,
    val raidLocation: Int? = null,
    val wipeId: UUID? = null,
)

sealed interface EventHistoryItem {
    val eventId: UUID
    val occurredAt: Instant

    data class ItemDrop(
        override val eventId: UUID,
        override val occurredAt: Instant,
        val itemId: UUID,
        val itemName: String,
        val itemRarity: String,
        val itemType: String,
        val droppedFor: String,
    ) : EventHistoryItem

    data class Booster(
        override val eventId: UUID,
        override val occurredAt: Instant,
        val boosterType: String,
        val activatedBy: String,
    ) : EventHistoryItem

    data class Global(
        override val eventId: UUID,
        override val occurredAt: Instant,
        val globalType: String,
    ) : EventHistoryItem

    data class Raid(
        override val eventId: UUID,
        override val occurredAt: Instant,
        val locations: List<Int>,
    ) : EventHistoryItem
}

data class EventHistoryPage(
    val items: List<EventHistoryItem>,
    val cursor: UUID?,
)

sealed interface EventHistoryResult {
    data class Available(
        val page: EventHistoryPage,
    ) : EventHistoryResult

    data object Unavailable : EventHistoryResult
}

sealed interface EventHistoryMenuScreenData {
    data class Feed(
        val filter: EventHistoryFilter,
        val search: String?,
        val sort: EventHistorySort,
        val itemRarity: EventHistoryItemRarity?,
        val itemType: EventHistoryItemType?,
        val boosterType: EventHistoryBoosterType?,
        val globalType: EventHistoryGlobalType?,
        val raidLocation: Int?,
        val raidLocationPage: Int,
        val wipeSelection: EventHistoryWipeSelection,
        val wipeOptions: EventHistoryWipeOptions,
        val result: EventHistoryResult,
        val hasPrevious: Boolean,
    ) : EventHistoryMenuScreenData

    data class SearchPrompt(
        val filter: EventHistoryFilter,
        val currentSearch: String?,
        val error: String?,
    ) : EventHistoryMenuScreenData
}
