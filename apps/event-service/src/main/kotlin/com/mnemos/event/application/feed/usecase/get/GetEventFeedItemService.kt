package com.mnemos.event.application.feed.usecase.get

import com.mnemos.event.application.feed.model.EventFeedItem
import com.mnemos.event.application.feed.port.out.EventFeedReader
import com.mnemos.event.domain.EventId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetEventFeedItemService(
    private val eventFeedReader: EventFeedReader,
) {
    fun execute(eventId: EventId): EventFeedItem? = eventFeedReader.findById(eventId)
}
