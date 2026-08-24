package com.mnemos.event.application.leaderboard.drop

import java.time.Instant

data class DropLeaderboardEntry(
    val nickname: String,
    val totalDrops: Long,
    val mythicalDrops: Long,
    val secretDrops: Long,
    val lastDropAt: Instant,
)
