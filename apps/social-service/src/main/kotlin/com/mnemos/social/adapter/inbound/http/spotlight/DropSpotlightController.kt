package com.mnemos.social.adapter.inbound.http.spotlight

import com.mnemos.social.adapter.inbound.http.spotlight.DropSpotlightApiMapper.toResponse
import com.mnemos.social.application.spotlight.CreateDropSpotlightService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/drop-spotlights")
class DropSpotlightController(
    private val service: CreateDropSpotlightService,
) {
    @PostMapping("/{itemDropEventId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(@PathVariable itemDropEventId: UUID): DropSpotlightResponse =
        service
            .execute(itemDropEventId)
            .toResponse()
}
