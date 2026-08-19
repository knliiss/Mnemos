package com.mnemos.event.adapter.out.persistence.feed

import com.mnemos.event.application.feed.model.BoosterFeedItem
import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedKind
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.application.feed.model.EventFeedSort
import com.mnemos.event.application.feed.model.GlobalFeedItem
import com.mnemos.event.application.feed.model.ItemDropFeedItem
import com.mnemos.event.application.feed.model.RaidFeedItem
import com.mnemos.event.application.feed.port.out.EventFeedReader
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.ItemId
import org.springframework.stereotype.Repository
import java.util.Locale

@Repository
class EventFeedJpaAdapter(
    private val repository: EventFeedJpaRepository,
) : EventFeedReader {
    override fun read(query: EventFeedQuery): List<EventFeedItem> {
        val searchTerm = query.search?.lowercase(Locale.ROOT)
        val droppedFor = query.droppedFor?.lowercase(Locale.ROOT)

        return repository
            .read(
                after = query.after?.id,
                eventId = query.eventId?.id,
                includeItemDrop = EventFeedKind.ITEM_DROP in query.kinds,
                includeBooster = EventFeedKind.BOOSTER in query.kinds,
                includeGlobal = EventFeedKind.GLOBAL in query.kinds,
                includeRaid = EventFeedKind.RAID in query.kinds,
                searchTerm = searchTerm,
                droppedFor = droppedFor,
                itemRarity = query.itemRarity?.name,
                newest = query.sort == EventFeedSort.NEWEST,
                limit = query.limit,
            ).map { projection -> projection.toModel() }
    }

    private fun EventFeedProjection.toModel(): EventFeedItem =
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

    private companion object {
        const val ITEM_DROP = "ITEM_DROP"
        const val BOOSTER = "BOOSTER"
        const val GLOBAL = "GLOBAL"
        const val RAID = "RAID"
    }
}
