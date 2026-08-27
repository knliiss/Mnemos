package com.mnemos.moderation.contracts.event

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import java.time.Instant
import java.util.UUID

data class LeaderboardExclusionChangedV1(
    val exclusionId: UUID,
    val subjectType: LeaderboardExclusionSubjectType,
    val subjectId: UUID,
    val scope: LeaderboardExclusionScope,
    val active: Boolean,
    val occurredAt: Instant,
)
