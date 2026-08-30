package com.mnemos.telegram.bot.adapter.out.event

import com.mnemos.event.contracts.internal.v1.InternalEventFeedResponse
import com.mnemos.event.contracts.internal.v1.InternalEventFeedSort
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.UUID

@FeignClient(
    name = "telegram-bot-event-service-client",
    url = "\${mnemos.clients.event-service.url}",
    configuration = [EventServiceFeignConfiguration::class],
)
interface EventServiceClient {
    @GetMapping("/internal/v1/events/feed")
    fun listEvents(
        @RequestParam(required = false)
        after: UUID?,
        @RequestParam(required = false)
        type: InternalEventFeedType?,
        @RequestParam(required = false)
        search: String?,
        @RequestParam
        sort: InternalEventFeedSort,
        @RequestParam
        limit: Int,
    ): InternalEventFeedResponse
}
