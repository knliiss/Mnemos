package com.mnemos.event.application.feed.usecase.list

import com.mnemos.event.application.feed.model.EventFeedPage
import com.mnemos.event.application.feed.port.out.EventFeedReader
import com.mnemos.event.domain.EventId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListEventFeedService(
    private val eventFeedReader: EventFeedReader,
) {
    fun execute(
        after: EventId?,
        limit: Int,
    ): EventFeedPage {
        val normalizedLimit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT)
        val readItems =
            eventFeedReader.read(
                after = after,
                limit = normalizedLimit,
            )
        val items =
            if (after == null) {
                readItems.asReversed()
            } else {
                readItems
            }

        return EventFeedPage(
            items = items,
            cursor = items.lastOrNull()?.eventId ?: after,
        )
    }

    private companion object {
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 100
    }
}
