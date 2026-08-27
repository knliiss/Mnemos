package com.mnemos.moderation.application.leaderboard.usecase.create

import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion

fun interface CreateLeaderboardExclusionUseCase {
    fun execute(command: CreateLeaderboardExclusionCommand): LeaderboardExclusion
}
