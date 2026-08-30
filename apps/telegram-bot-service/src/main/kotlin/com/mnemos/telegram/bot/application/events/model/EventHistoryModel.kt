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

data class EventHistoryQuery(
    val filter: EventHistoryFilter,
    val after: UUID?,
    val search: String? = null,
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

data class EventHistoryMenuScreenData(
    val filter: EventHistoryFilter,
    val result: EventHistoryResult,
    val hasPrevious: Boolean,
)
