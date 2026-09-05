package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import java.time.Instant
import java.util.UUID

enum class EventFeedType {
    ITEM_DROP,
    BOOSTER,
    GLOBAL,
    RAID,
}

sealed interface EventFeedItemResponse {
    val type: EventFeedType
    val eventId: UUID
    val occurredAt: Instant
}

data class ItemDropFeedItemResponse(
    override val type: EventFeedType = EventFeedType.ITEM_DROP,
    override val eventId: UUID,
    override val occurredAt: Instant,
    val itemId: UUID,
    val itemKey: String?,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: String,
    val droppedFor: String,
) : EventFeedItemResponse

data class BoosterFeedItemResponse(
    override val type: EventFeedType = EventFeedType.BOOSTER,
    override val eventId: UUID,
    override val occurredAt: Instant,
    val boosterType: DomainBoosterType,
    val activatedBy: String,
) : EventFeedItemResponse

data class GlobalFeedItemResponse(
    override val type: EventFeedType = EventFeedType.GLOBAL,
    override val eventId: UUID,
    override val occurredAt: Instant,
    val globalType: DomainGlobalEventType,
) : EventFeedItemResponse

data class RaidFeedItemResponse(
    override val type: EventFeedType = EventFeedType.RAID,
    override val eventId: UUID,
    override val occurredAt: Instant,
    val locations: List<Int>,
) : EventFeedItemResponse

data class EventFeedResponse(
    val items: List<EventFeedItemResponse>,
    val cursor: UUID?,
)
