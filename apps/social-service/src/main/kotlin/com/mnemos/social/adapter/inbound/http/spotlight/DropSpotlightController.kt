package com.mnemos.social.adapter.inbound.http.spotlight

import com.mnemos.social.application.spotlight.CreateDropSpotlightService
import com.mnemos.social.domain.spotlight.model.DropSpotlight
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

data class DropSpotlightResponse(
    val id: UUID,
    val itemDropEventId: UUID,
    val sourceAccountId: UUID,
    val pricePrime: Long,
    val scheduledAt: Instant,
    val status: String,
)

@RestController
@RequestMapping("/api/v1/drop-spotlights")
class DropSpotlightController(
    private val service: CreateDropSpotlightService,
) {
    @PostMapping("/{itemDropEventId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@PathVariable itemDropEventId: UUID): DropSpotlightResponse =
        service.execute(itemDropEventId).toResponse()

    private fun DropSpotlight.toResponse(): DropSpotlightResponse =
        DropSpotlightResponse(
            id = id.id,
            itemDropEventId = itemDrop.id,
            sourceAccountId = sourceAccountId,
            pricePrime = pricePrime,
            scheduledAt = scheduledAt,
            status = status.name,
        )
}
