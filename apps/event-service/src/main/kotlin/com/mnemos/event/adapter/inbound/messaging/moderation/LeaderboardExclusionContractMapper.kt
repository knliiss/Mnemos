package com.mnemos.event.adapter.inbound.messaging.moderation

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import com.mnemos.moderation.contracts.event.LeaderboardExclusionChangedV1

object LeaderboardExclusionContractMapper {
    fun LeaderboardExclusionChangedV1.toChange(): LeaderboardExclusionChange =
        LeaderboardExclusionChange(
            exclusionId = exclusionId,
            subjectType = LeaderboardExclusionSubjectType.valueOf(subjectType.name),
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.valueOf(scope.name),
            active = active,
            occurredAt = occurredAt,
        )
}
