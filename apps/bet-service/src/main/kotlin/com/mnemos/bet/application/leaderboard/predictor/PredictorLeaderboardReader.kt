package com.mnemos.bet.application.leaderboard.predictor

import java.time.Instant

interface PredictorLeaderboardReader {
    fun read(
        from: Instant,
        to: Instant?,
        sort: PredictorLeaderboardSort,
        minimumSample: Int,
        limit: Int,
    ): List<PredictorLeaderboardCandidate>
}
