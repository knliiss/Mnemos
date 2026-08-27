package com.mnemos.event.adapter.inbound.http.leaderboard

import java.time.Instant
import java.util.UUID

data class BoosterLeaderboardResponse(
    val rank: Int,
    val playerId: UUID,
    val nickname: String,
    val totalBoosters: Long,
    val lastActivatedAt: Instant,
)
