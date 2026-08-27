package com.mnemos.moderation.adapter.out.persistence.leaderboard

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionId

object LeaderboardExclusionPersistenceMapper {
    fun LeaderboardExclusion.toEntity(): LeaderboardExclusionEntity =
        LeaderboardExclusionEntity(
            id = id.id,
            subjectType = subjectType,
            subjectId = subjectId,
            scope = scope,
            reason = reason,
            createdByUserId = createdByUserId.id,
            createdAt = createdAt,
        )

    fun LeaderboardExclusionEntity.toDomain(): LeaderboardExclusion =
        LeaderboardExclusion(
            id = LeaderboardExclusionId(id),
            subjectType = subjectType,
            subjectId = subjectId,
            scope = scope,
            reason = reason,
            createdByUserId = UserId(createdByUserId),
            createdAt = createdAt,
        )
}
