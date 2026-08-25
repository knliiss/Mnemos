package com.mnemos.event.application.leaderboard.drop

import java.time.Instant

interface DropLeaderboardReader {
    fun read(
        from: Instant,
        to: Instant?,
        query: DropLeaderboardQuery,
    ): List<DropLeaderboardEntry>
}
