package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.adapter.inbound.http.feed.EventFeedApiMapper.toQuery
import com.mnemos.event.adapter.inbound.http.feed.EventFeedApiMapper.toResponse
import com.mnemos.event.application.feed.usecase.get.GetEventFeedItemService
import com.mnemos.event.application.feed.usecase.list.ListEventFeedService
import com.mnemos.event.domain.EventId
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/events/feed")
class EventFeedController(
    private val listEventFeedService: ListEventFeedService,
    private val getEventFeedItemService: GetEventFeedItemService,
) {
    @GetMapping
    fun list(
        @Valid @ModelAttribute request: ListEventFeedRequest,
    ): EventFeedResponse = listEventFeedService.execute(request.toQuery()).toResponse()

    @GetMapping("/{eventId}")
    fun get(
        @PathVariable
        eventId: UUID,
    ): ResponseEntity<EventFeedItemResponse> {
        val item =
            getEventFeedItemService.execute(EventId(eventId))
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(item.toResponse())
    }
}
