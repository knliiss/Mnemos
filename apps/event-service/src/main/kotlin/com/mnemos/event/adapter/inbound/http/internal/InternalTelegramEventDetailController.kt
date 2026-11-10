package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.adapter.inbound.http.internal.InternalEventFeedApiMapper.toInternalResponse
import com.mnemos.event.application.feed.usecase.get.GetEventFeedItemService
import com.mnemos.event.contracts.internal.v1.InternalEventFeedItemResponse
import com.mnemos.event.domain.EventId
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/internal/v1/events/details")
@PreAuthorize("authentication.name == 'telegram-bot-service'")
class InternalTelegramEventDetailController(
    private val getEventFeedItem: GetEventFeedItemService,
) {
    @GetMapping("/{eventId}")
    fun get(@PathVariable eventId: UUID): ResponseEntity<InternalEventFeedItemResponse> {
        val item = getEventFeedItem.execute(EventId(eventId)) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(item.toInternalResponse())
    }
}
