package com.mnemos.event.application.feed.usecase.list

import com.mnemos.event.application.feed.model.EventFeedPage
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.application.feed.port.out.EventFeedReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListEventFeedService(
    private val eventFeedReader: EventFeedReader,
) {
    fun execute(query: EventFeedQuery): EventFeedPage {
        val normalizedQuery =
            query.copy(
                limit = query.limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
                search = query.search?.trim()?.takeIf { value -> value.isNotEmpty() },
                droppedFor = query.droppedFor?.trim()?.takeIf { value -> value.isNotEmpty() },
            )

        val items = eventFeedReader.read(normalizedQuery)

        return EventFeedPage(
            items = items,
            cursor = items.lastOrNull()?.eventId ?: normalizedQuery.after,
        )
    }

    private companion object {
        const val MIN_LIMIT = 1
        const val MAX_LIMIT = 100
    }
}
