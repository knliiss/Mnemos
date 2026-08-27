package com.mnemos.moderation.application.leaderboard.usecase.create

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import java.util.UUID

data class CreateLeaderboardExclusionCommand(
    val subjectType: LeaderboardExclusionSubjectType,
    val subjectId: UUID,
    val scope: LeaderboardExclusionScope,
    val reason: String?,
)
