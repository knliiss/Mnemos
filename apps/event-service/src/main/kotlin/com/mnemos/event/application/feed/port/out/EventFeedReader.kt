package com.mnemos.event.application.feed.port.out

import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.domain.EventId

interface EventFeedReader {
    fun read(
        after: EventId?,
        limit: Int,
    ): List<EventFeedItem>
}
