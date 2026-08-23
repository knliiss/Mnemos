package com.mnemos.moderation.adapter.inbound.http.internal

import com.mnemos.moderation.application.sanction.usecase.active.GetActiveBansUseCase
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/moderation")
@PreAuthorize("authentication.name == 'gateway-service'")
class ModerationInternalController(
    private val getActiveBansUseCase: GetActiveBansUseCase,
) {
    @GetMapping("/active-bans")
    fun activeBans(): List<ActiveBanResponse> =
        getActiveBansUseCase
            .execute()
            .map { ban ->
                ActiveBanResponse(
                    sanctionId = ban.sanctionId.id,
                    userId = ban.userId.id,
                    occurredAt = ban.occurredAt,
                )
            }
}
