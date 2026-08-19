package com.mnemos.event.adapter.out.persistence.feed

import com.mnemos.event.application.feed.model.BoosterFeedItem
import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.GlobalFeedItem
import com.mnemos.event.application.feed.model.ItemDropFeedItem
import com.mnemos.event.application.feed.model.RaidFeedItem
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.ItemId

object EventFeedPersistenceMapper {
    fun EventFeedProjection.toModel(): EventFeedItem =
        when (getEventType()) {
            ITEM_DROP ->
                ItemDropFeedItem(
                    eventId = EventId(getEventId()),
                    occurredAt = getOccurredAt(),
                    itemId = ItemId(requireNotNull(getItemId())),
                    itemName = requireNotNull(getItemName()),
                    itemRarity = DomainItemRarity.valueOf(requireNotNull(getItemRarity())),
                    itemType = requireNotNull(getItemType()),
                    droppedFor = requireNotNull(getDroppedFor()),
                )

            BOOSTER ->
                BoosterFeedItem(
                    eventId = EventId(getEventId()),
                    occurredAt = getOccurredAt(),
                    boosterType = DomainBoosterType.valueOf(requireNotNull(getBoosterType())),
                    activatedBy = requireNotNull(getActivatedBy()),
                )

            GLOBAL ->
                GlobalFeedItem(
                    eventId = EventId(getEventId()),
                    occurredAt = getOccurredAt(),
                    globalType = DomainGlobalEventType.valueOf(requireNotNull(getGlobalType())),
                )

            RAID ->
                RaidFeedItem(
                    eventId = EventId(getEventId()),
                    occurredAt = getOccurredAt(),
                    locations =
                        getRaidLocations()
                            ?.takeIf { locations -> locations.isNotBlank() }
                            ?.split(',')
                            ?.map { location -> location.toInt() }
                            .orEmpty(),
                )

            else -> error("Unsupported event feed type: ${getEventType()}")
        }

    private const val ITEM_DROP = "ITEM_DROP"
    private const val BOOSTER = "BOOSTER"
    private const val GLOBAL = "GLOBAL"
    private const val RAID = "RAID"
}
