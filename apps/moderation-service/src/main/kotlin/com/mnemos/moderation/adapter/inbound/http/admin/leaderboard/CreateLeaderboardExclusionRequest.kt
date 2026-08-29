package com.mnemos.moderation.adapter.inbound.http.admin.leaderboard

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionScopePolicy
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size
import java.util.UUID

data class CreateLeaderboardExclusionRequest(
    val subjectType: LeaderboardExclusionSubjectType,
    val subjectId: UUID,
    val scope: LeaderboardExclusionScope,
    @field:Size(max = 256)
    val reason: String? = null,
) {
    @get:AssertTrue(message = "scope is not supported for subjectType")
    val subjectScopeSupported: Boolean
        get() = LeaderboardExclusionScopePolicy.supports(subjectType, scope)
}
