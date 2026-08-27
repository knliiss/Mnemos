package com.mnemos.moderation.application.leaderboard.usecase.list

import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion

fun interface ListLeaderboardExclusionsUseCase {
    fun execute(): List<LeaderboardExclusion>
}
