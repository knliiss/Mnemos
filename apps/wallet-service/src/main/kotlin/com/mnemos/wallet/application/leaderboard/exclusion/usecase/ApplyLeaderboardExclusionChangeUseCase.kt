package com.mnemos.wallet.application.leaderboard.exclusion.usecase

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange

fun interface ApplyLeaderboardExclusionChangeUseCase {
    fun execute(change: LeaderboardExclusionChange)
}
