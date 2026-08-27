package com.mnemos.event.adapter.inbound.http.leaderboard

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardEntry

object BoosterLeaderboardApiMapper {
    fun BoosterLeaderboardEntry.toResponse(rank: Int): BoosterLeaderboardResponse =
        BoosterLeaderboardResponse(
            rank = rank,
            playerId = playerId.id,
            nickname = nickname,
            totalBoosters = totalBoosters,
            lastActivatedAt = lastActivatedAt,
        )
}
