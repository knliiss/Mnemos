package com.mnemos.event.adapter.inbound.http.leaderboard

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

data class BoosterLeaderboardRequest(
    val wipeId: UUID? = null,
    @field:Min(3)
    @field:Max(100)
    val limit: Int = 100,
)
