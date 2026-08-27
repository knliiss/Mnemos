package com.mnemos.event.application.leaderboard.drop

import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Instant

data class DropLeaderboardEntry(
    val playerId: CristalixPlayerId,
    val nickname: String,
    val totalDrops: Long,
    val mythicalDrops: Long,
    val secretDrops: Long,
    val lastDropAt: Instant,
)
