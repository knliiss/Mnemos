package com.mnemos.moderation.adapter.inbound.http.internal

import com.mnemos.moderation.application.sanction.usecase.active.GetActiveBansUseCase
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/v1/moderation")
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
