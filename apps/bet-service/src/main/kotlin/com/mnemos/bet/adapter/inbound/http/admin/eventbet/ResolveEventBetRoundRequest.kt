package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import jakarta.validation.constraints.NotBlank

data class ResolveEventBetRoundRequest(
    val outcome: EventBetOutcome,
    @field:NotBlank
    val reason: String,
)
