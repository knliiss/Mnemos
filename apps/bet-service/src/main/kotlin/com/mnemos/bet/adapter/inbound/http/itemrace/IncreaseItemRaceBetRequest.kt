package com.mnemos.bet.adapter.inbound.http.itemrace

import jakarta.validation.constraints.Positive
import java.util.UUID

data class IncreaseItemRaceBetRequest(
    val raceId: UUID,
    @field:Positive
    val amount: Long,
)
