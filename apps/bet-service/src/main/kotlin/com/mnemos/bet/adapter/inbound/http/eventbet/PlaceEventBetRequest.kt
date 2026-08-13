package com.mnemos.bet.adapter.inbound.http.eventbet

import com.mnemos.bet.contracts.shared.BetSide
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

data class PlaceEventBetRequest(
    val roundId: UUID,
    val side: BetSide,
    @field:Min(100)
    @field:Max(5000)
    val amount: Long,
)
