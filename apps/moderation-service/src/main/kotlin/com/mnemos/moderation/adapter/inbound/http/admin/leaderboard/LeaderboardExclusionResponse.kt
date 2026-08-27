package com.mnemos.moderation.adapter.inbound.http.admin.leaderboard

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import java.time.Instant
import java.util.UUID

data class LeaderboardExclusionResponse(
    val id: UUID,
    val subjectType: LeaderboardExclusionSubjectType,
    val subjectId: UUID,
    val scope: LeaderboardExclusionScope,
    val reason: String?,
    val createdByUserId: UUID,
    val createdAt: Instant,
)
