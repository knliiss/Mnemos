package com.mnemos.moderation.adapter.out.persistence.leaderboard

import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface LeaderboardExclusionJpaRepository : JpaRepository<LeaderboardExclusionEntity, UUID> {
    fun findBySubjectTypeAndSubjectIdAndScope(
        subjectType: LeaderboardExclusionSubjectType,
        subjectId: UUID,
        scope: LeaderboardExclusionScope,
    ): LeaderboardExclusionEntity?

    fun findAllByOrderByCreatedAtDesc(): List<LeaderboardExclusionEntity>
}
