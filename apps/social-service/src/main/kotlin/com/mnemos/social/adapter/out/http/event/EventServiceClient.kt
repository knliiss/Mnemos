package com.mnemos.social.adapter.out.http.event

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.time.Instant
import java.util.UUID

@FeignClient(
    name = "social-event-service-client",
    url = "\${mnemos.social.event-service.base-url}",
)
interface EventServiceClient {
    @GetMapping("/internal/v1/item-drops/{itemDropEventId}")
    fun getItemDrop(@PathVariable itemDropEventId: UUID): InternalItemDropResponse?
}

data class InternalItemDropResponse(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: String,
    val itemType: String,
    val droppedFor: String,
    val occurredAt: Instant,
)
