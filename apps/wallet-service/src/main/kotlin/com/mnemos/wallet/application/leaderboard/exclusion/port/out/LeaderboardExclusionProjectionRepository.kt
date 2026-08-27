package com.mnemos.wallet.application.leaderboard.exclusion.port.out

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange

fun interface LeaderboardExclusionProjectionRepository {
    fun apply(change: LeaderboardExclusionChange)
}
