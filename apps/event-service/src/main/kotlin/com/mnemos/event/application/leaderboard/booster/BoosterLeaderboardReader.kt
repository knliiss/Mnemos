package com.mnemos.event.application.leaderboard.booster

import java.time.Instant

interface BoosterLeaderboardReader {
    fun read(
        from: Instant,
        to: Instant?,
        limit: Int,
    ): List<BoosterLeaderboardEntry>
}
