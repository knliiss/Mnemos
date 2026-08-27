package com.mnemos.moderation.adapter.inbound.http.admin.leaderboard

import com.mnemos.moderation.application.leaderboard.usecase.create.CreateLeaderboardExclusionCommand
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion

object LeaderboardExclusionApiMapper {
    fun CreateLeaderboardExclusionRequest.toCommand(): CreateLeaderboardExclusionCommand =
        CreateLeaderboardExclusionCommand(
            subjectType = subjectType,
            subjectId = subjectId,
            scope = scope,
            reason = reason,
        )

    fun LeaderboardExclusion.toResponse(): LeaderboardExclusionResponse =
        LeaderboardExclusionResponse(
            id = id.id,
            subjectType = subjectType,
            subjectId = subjectId,
            scope = scope,
            reason = reason,
            createdByUserId = createdByUserId.id,
            createdAt = createdAt,
        )
}
