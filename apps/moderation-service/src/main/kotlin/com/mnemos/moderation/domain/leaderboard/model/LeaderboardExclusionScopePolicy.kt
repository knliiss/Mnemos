package com.mnemos.moderation.domain.leaderboard.model

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType

object LeaderboardExclusionScopePolicy {
    fun supports(
        subjectType: LeaderboardExclusionSubjectType,
        scope: LeaderboardExclusionScope,
    ): Boolean =
        when (subjectType) {
            LeaderboardExclusionSubjectType.ACCOUNT ->
                scope == LeaderboardExclusionScope.ALL || scope == LeaderboardExclusionScope.COINS

            LeaderboardExclusionSubjectType.PLAYER ->
                scope == LeaderboardExclusionScope.ALL ||
                    scope == LeaderboardExclusionScope.DROPS ||
                    scope == LeaderboardExclusionScope.BOOSTERS
        }
}
