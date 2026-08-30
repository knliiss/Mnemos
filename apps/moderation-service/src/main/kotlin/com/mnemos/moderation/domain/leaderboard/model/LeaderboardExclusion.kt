package com.mnemos.moderation.domain.leaderboard.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import java.time.Instant
import java.util.UUID

data class LeaderboardExclusion(
    val id: LeaderboardExclusionId,
    val subjectType: LeaderboardExclusionSubjectType,
    val subjectId: UUID,
    val scope: LeaderboardExclusionScope,
    val reason: String?,
    val createdByUserId: UserId,
    val createdAt: Instant,
) {
    companion object {
        fun create(
            subjectType: LeaderboardExclusionSubjectType,
            subjectId: UUID,
            scope: LeaderboardExclusionScope,
            reason: String?,
            createdByUserId: UserId,
            createdAt: Instant,
        ): LeaderboardExclusion {
            require(LeaderboardExclusionScopePolicy.supports(subjectType, scope)) {
                "Scope $scope is not supported for leaderboard exclusion subject type $subjectType"
            }

            return LeaderboardExclusion(
                id = LeaderboardExclusionId.generate(),
                subjectType = subjectType,
                subjectId = subjectId,
                scope = scope,
                reason = reason?.trim()?.takeIf(String::isNotBlank),
                createdByUserId = createdByUserId,
                createdAt = createdAt,
            )
        }
    }
}
