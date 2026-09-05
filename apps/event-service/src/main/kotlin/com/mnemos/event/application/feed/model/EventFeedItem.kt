package com.mnemos.event.application.feed.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.ItemId
import java.time.Instant

sealed interface EventFeedItem {
    val eventId: EventId
    val occurredAt: Instant
}

data class ItemDropFeedItem(
    override val eventId: EventId,
    override val occurredAt: Instant,
    val itemId: ItemId,
    val itemKey: String? = null,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: String,
    val droppedFor: String,
) : EventFeedItem

data class BoosterFeedItem(
    override val eventId: EventId,
    override val occurredAt: Instant,
    val boosterType: DomainBoosterType,
    val activatedBy: String,
) : EventFeedItem

data class GlobalFeedItem(
    override val eventId: EventId,
    override val occurredAt: Instant,
    val globalType: DomainGlobalEventType,
) : EventFeedItem

data class RaidFeedItem(
    override val eventId: EventId,
    override val occurredAt: Instant,
    val locations: List<Int>,
) : EventFeedItem

data class EventFeedPage(
    val items: List<EventFeedItem>,
    val cursor: EventId?,
)
