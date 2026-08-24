package com.mnemos.bet.application.leaderboard.predictor

import java.util.UUID

data class PredictorLeaderboardQuery(
    val wipeId: UUID?,
    val sort: PredictorLeaderboardSort,
    val minimumSample: Int,
    val limit: Int,
)
