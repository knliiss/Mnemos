package com.mnemos.event.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import java.io.Serializable
import java.util.UUID

data class LeaderboardExclusionProjectionId(
    var subjectType: LeaderboardExclusionSubjectType = LeaderboardExclusionSubjectType.PLAYER,
    var subjectId: UUID = UUID(0, 0),
    var scope: LeaderboardExclusionScope = LeaderboardExclusionScope.ALL,
) : Serializable
