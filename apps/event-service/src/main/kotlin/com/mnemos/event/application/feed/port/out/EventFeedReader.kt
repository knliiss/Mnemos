package com.mnemos.event.application.feed.port.out

import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.domain.EventId

interface EventFeedReader {
    fun read(query: EventFeedQuery): List<EventFeedItem>

    fun findById(eventId: EventId): EventFeedItem?
}
