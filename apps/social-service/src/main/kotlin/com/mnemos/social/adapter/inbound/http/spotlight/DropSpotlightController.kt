package com.mnemos.social.adapter.inbound.http.spotlight

import com.mnemos.common.web.logging.MnemosHeaders.IDEMPOTENCY_KEY
import com.mnemos.social.adapter.inbound.http.spotlight.DropSpotlightApiMapper.toResponse
import com.mnemos.social.application.spotlight.usecase.create.CreateDropSpotlightUseCase
import com.mnemos.social.application.spotlight.usecase.get.GetDropSpotlightService
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/drop-spotlights")
class DropSpotlightController(
    private val createDropSpotlightUseCase: CreateDropSpotlightUseCase,
    private val getDropSpotlightService: GetDropSpotlightService,
) {
    @PostMapping("/{itemDropEventId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(
        @RequestHeader(IDEMPOTENCY_KEY) idempotencyKey: UUID,
        @PathVariable itemDropEventId: UUID,
    ): DropSpotlightResponse =
        createDropSpotlightUseCase
            .execute(
                idempotencyKey = idempotencyKey,
                itemDropEventId = itemDropEventId,
            ).toResponse()

    @GetMapping("/{spotlightId}")
    fun get(
        @PathVariable spotlightId: UUID,
    ): DropSpotlightResponse =
        getDropSpotlightService
            .execute(DropSpotlightId(spotlightId))
            .toResponse()
}
