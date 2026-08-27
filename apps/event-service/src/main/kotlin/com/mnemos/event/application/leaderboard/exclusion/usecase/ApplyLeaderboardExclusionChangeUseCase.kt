package com.mnemos.event.application.leaderboard.exclusion.usecase

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange

fun interface ApplyLeaderboardExclusionChangeUseCase {
    fun execute(change: LeaderboardExclusionChange)
}
