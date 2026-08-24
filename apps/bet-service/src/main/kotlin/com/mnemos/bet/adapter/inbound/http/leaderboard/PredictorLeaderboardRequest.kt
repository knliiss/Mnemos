package com.mnemos.bet.adapter.inbound.http.leaderboard

import com.mnemos.bet.application.leaderboard.predictor.PredictorLeaderboardSort
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.util.UUID

data class PredictorLeaderboardRequest(
    val wipeId: UUID? = null,
    val sort: PredictorLeaderboardSort = PredictorLeaderboardSort.PROFIT,
    @field:Min(1)
    @field:Max(1_000)
    val minimumSample: Int = 10,
    @field:Min(3)
    @field:Max(100)
    val limit: Int = 50,
)
