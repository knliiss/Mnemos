package com.mnemos.event.application.feed.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.wipe.model.WipeId
import java.time.Instant

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
    val wipeId: WipeId? = null,
    val from: Instant? = null,
    val to: Instant? = null,
)
