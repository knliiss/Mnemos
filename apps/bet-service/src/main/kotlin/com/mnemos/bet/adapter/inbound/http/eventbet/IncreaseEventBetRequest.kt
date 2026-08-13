package com.mnemos.bet.adapter.inbound.http.eventbet

import jakarta.validation.constraints.Positive
import java.util.UUID

data class IncreaseEventBetRequest(
    val roundId: UUID,
    @field:Positive
    val amount: Long,
)
