package com.mnemos.event.application.feed.port.out

import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.model.EventFeedQuery

interface EventFeedReader {
    fun read(query: EventFeedQuery): List<EventFeedItem>
}
