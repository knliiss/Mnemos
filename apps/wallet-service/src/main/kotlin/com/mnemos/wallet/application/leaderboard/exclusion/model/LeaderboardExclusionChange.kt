package com.mnemos.wallet.application.leaderboard.exclusion.model

import java.time.Instant
import java.util.UUID

data class LeaderboardExclusionChange(
    val exclusionId: UUID,
    val subjectType: LeaderboardExclusionSubjectType,
    val subjectId: UUID,
    val scope: LeaderboardExclusionScope,
    val active: Boolean,
    val occurredAt: Instant,
)
