package com.mnemos.event.adapter.inbound.http.leaderboard

import java.time.Instant

data class DropLeaderboardResponse(
    val rank: Int,
    val nickname: String,
    val totalDrops: Long,
    val mythicalDrops: Long,
    val secretDrops: Long,
    val lastDropAt: Instant,
)
