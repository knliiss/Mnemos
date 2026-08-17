package com.mnemos.event.adapter.inbound.http.feed

import com.mnemos.event.adapter.inbound.http.feed.EventFeedApiMapper.toResponse
import com.mnemos.event.application.feed.usecase.list.ListEventFeedService
import com.mnemos.event.domain.EventId
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
        @RequestParam(defaultValue = "50")
        @Min(1)
        @Max(100)
        limit: Int,
    ): EventFeedResponse =
        listEventFeedService
            .execute(
                after = after?.let(::EventId),
                limit = limit,
            ).toResponse()
}
