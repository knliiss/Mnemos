package com.mnemos.event.adapter.out.persistence.feed

import com.mnemos.event.adapter.out.persistence.feed.EventFeedPersistenceMapper.toModel
import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedKind
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.application.feed.model.EventFeedSort
import com.mnemos.event.application.feed.port.out.EventFeedReader
import com.mnemos.event.domain.EventId
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
                eventId = null,
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

    override fun findById(eventId: EventId): EventFeedItem? =
        repository
            .read(
                after = null,
                eventId = eventId.id,
                includeItemDrop = true,
                includeBooster = true,
                includeGlobal = true,
                includeRaid = true,
                searchTerm = null,
                droppedFor = null,
                itemRarity = null,
                newest = true,
                limit = 1,
            ).firstOrNull()
            ?.toModel()
}
