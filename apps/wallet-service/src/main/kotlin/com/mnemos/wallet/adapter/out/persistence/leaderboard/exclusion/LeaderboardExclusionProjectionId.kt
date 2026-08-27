package com.mnemos.wallet.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import java.io.Serializable
import java.util.UUID

data class LeaderboardExclusionProjectionId(
    var subjectType: LeaderboardExclusionSubjectType = LeaderboardExclusionSubjectType.ACCOUNT,
    var subjectId: UUID = UUID(0, 0),
    var scope: LeaderboardExclusionScope = LeaderboardExclusionScope.ALL,
) : Serializable
