package com.mnemos.event.application.leaderboard.exclusion.port.out

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange

fun interface LeaderboardExclusionProjectionRepository {
    fun apply(change: LeaderboardExclusionChange)
}
