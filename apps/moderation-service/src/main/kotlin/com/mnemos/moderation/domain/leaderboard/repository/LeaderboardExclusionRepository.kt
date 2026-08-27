package com.mnemos.moderation.domain.leaderboard.repository

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionId
import java.util.UUID

interface LeaderboardExclusionRepository {
    fun save(exclusion: LeaderboardExclusion): LeaderboardExclusion

    fun findById(id: LeaderboardExclusionId): LeaderboardExclusion?

    fun findBySubjectAndScope(
        subjectType: LeaderboardExclusionSubjectType,
        subjectId: UUID,
        scope: LeaderboardExclusionScope,
    ): LeaderboardExclusion?

    fun findAll(): List<LeaderboardExclusion>

    fun delete(exclusion: LeaderboardExclusion)
}
