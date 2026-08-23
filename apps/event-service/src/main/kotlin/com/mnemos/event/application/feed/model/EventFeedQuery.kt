package com.mnemos.event.application.feed.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.DomainItemRarity

enum class EventFeedKind {
    ITEM_DROP,
    BOOSTER,
    GLOBAL,
    RAID,
}

enum class EventFeedSort {
    NEWEST,
    OLDEST,
}

data class EventFeedQuery(
    val after: EventId?,
    val limit: Int,
    val kinds: Set<EventFeedKind> = EventFeedKind.entries.toSet(),
    val search: String? = null,
    val droppedFor: String? = null,
    val itemRarity: DomainItemRarity? = null,
    val sort: EventFeedSort = EventFeedSort.NEWEST,
)
