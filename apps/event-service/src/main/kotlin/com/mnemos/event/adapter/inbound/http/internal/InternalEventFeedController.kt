package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalEventFeedApiMapper.toInternalResponse
import com.mnemos.event.adapter.inbound.http.internal.InternalEventFeedApiMapper.toQuery
import com.mnemos.event.application.feed.usecase.list.ListEventFeedService
import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@Validated
@RestController
@RequestMapping("/internal/v1/events/feed")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalEventFeedController(
    private val listEventFeedService: ListEventFeedService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false)
        after: UUID?,
        @RequestParam(required = false)
        type: InternalEventFeedType?,
        @RequestParam(required = false)
        @Size(max = 64)
        search: String?,
        @RequestParam(defaultValue = "NEWEST")
        sort: InternalEventFeedSort,
        @RequestParam(defaultValue = "8")
        @Min(1)
        @Max(100)
        limit: Int,
    ): InternalEventFeedResponse =
        listEventFeedService
            .execute(
                toQuery(
                    after = after,
                    type = type,
                    search = search,
                    sort = sort,
                    limit = limit,
                ),
            ).toInternalResponse()
}
