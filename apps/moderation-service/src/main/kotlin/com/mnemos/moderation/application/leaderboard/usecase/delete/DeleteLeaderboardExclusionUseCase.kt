package com.mnemos.moderation.application.leaderboard.usecase.delete

import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionId

fun interface DeleteLeaderboardExclusionUseCase {
    fun execute(id: LeaderboardExclusionId)
}
