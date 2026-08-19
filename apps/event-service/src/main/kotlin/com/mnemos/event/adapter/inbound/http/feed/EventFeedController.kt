package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.adapter.inbound.http.feed.EventFeedApiMapper.toResponse
import com.mnemos.event.application.feed.model.EventFeedKind
import com.mnemos.event.application.feed.model.EventFeedQuery
import com.mnemos.event.application.feed.model.EventFeedSort
import com.mnemos.event.application.feed.usecase.list.ListEventFeedService
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.DomainItemRarity
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@Validated
@RequestMapping("/api/v1/events/feed")
class EventFeedController(
    private val listEventFeedService: ListEventFeedService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) after: UUID?,
        @RequestParam(name = "type", required = false) types: Set<EventFeedKind>?,
        @RequestParam(required = false)
        @Size(max = 64)
        search: String?,
        @RequestParam(required = false)
        @Size(min = 4, max = 20)
        droppedFor: String?,
        @RequestParam(required = false) itemRarity: DomainItemRarity?,
        @RequestParam(defaultValue = "NEWEST") sort: EventFeedSort,
        @RequestParam(defaultValue = "50")
        @Min(1)
        @Max(100)
        limit: Int,
    ): EventFeedResponse =
        listEventFeedService
            .execute(
                EventFeedQuery(
                    after = after?.let(::EventId),
                    limit = limit,
                    kinds = types?.takeIf { values -> values.isNotEmpty() } ?: EventFeedKind.entries.toSet(),
                    search = search,
                    droppedFor = droppedFor,
                    itemRarity = itemRarity,
                    sort = sort,
                ),
            ).toResponse()
}
