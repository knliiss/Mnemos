package com.mnemos.event.adapter.inbound.http.leaderboard

import java.time.Instant
import java.util.UUID

data class DropLeaderboardResponse(
    val rank: Int,
    val playerId: UUID,
    val nickname: String,
    val totalDrops: Long,
    val mythicalDrops: Long,
    val secretDrops: Long,
    val lastDropAt: Instant,
)
