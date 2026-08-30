package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedItemResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage

object EventHistoryMapper {
    fun InternalEventFeedResponse.toModel(): EventHistoryPage =
        EventHistoryPage(
            items = items.map { item -> item.toModel() },
            cursor = cursor,
        )

    private fun InternalEventFeedItemResponse.toModel(): EventHistoryItem =
        when (type) {
            InternalEventFeedType.ITEM_DROP ->
                EventHistoryItem.ItemDrop(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    itemId = requireNotNull(itemId),
                    itemName = requireNotNull(itemName),
                    itemRarity = requireNotNull(itemRarity).name,
                    itemType = requireNotNull(itemType),
                    droppedFor = requireNotNull(droppedFor),
                )

            InternalEventFeedType.BOOSTER ->
                EventHistoryItem.Booster(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    boosterType = requireNotNull(boosterType).name,
                    activatedBy = requireNotNull(activatedBy),
                )

            InternalEventFeedType.GLOBAL ->
                EventHistoryItem.Global(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    globalType = requireNotNull(globalType).name,
                )

            InternalEventFeedType.RAID ->
                EventHistoryItem.Raid(
                    eventId = eventId,
                    occurredAt = occurredAt,
                    locations = locations,
                )
        }
}
