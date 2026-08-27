package com.mnemos.event.application.leaderboard.booster

import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Instant

data class BoosterLeaderboardEntry(
    val playerId: CristalixPlayerId,
    val nickname: String,
    val totalBoosters: Long,
    val lastActivatedAt: Instant,
)
