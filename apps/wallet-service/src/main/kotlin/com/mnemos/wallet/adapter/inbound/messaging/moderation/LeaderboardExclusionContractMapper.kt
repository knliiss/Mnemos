package com.mnemos.wallet.adapter.inbound.messaging.moderation

import com.mnemos.moderation.contracts.event.LeaderboardExclusionChangedV1
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType

object LeaderboardExclusionContractMapper {
    fun LeaderboardExclusionChangedV1.toChange(): LeaderboardExclusionChange =
        LeaderboardExclusionChange(
            subjectType = LeaderboardExclusionSubjectType.valueOf(subjectType.name),
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.valueOf(scope.name),
            active = active,
            occurredAt = occurredAt,
        )
}
