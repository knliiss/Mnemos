package com.mnemos.wallet.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.springframework.stereotype.Repository

@Repository
class LeaderboardExclusionProjectionRepositoryAdapter(
    private val repository: LeaderboardExclusionProjectionJpaRepository,
) : LeaderboardExclusionProjectionRepository {
    override fun apply(change: LeaderboardExclusionChange) {
        val id =
            LeaderboardExclusionProjectionId(
                subjectType = change.subjectType,
                subjectId = change.subjectId,
                scope = change.scope,
            )
        val existing = repository.findById(id).orElse(null)

        if (existing != null && existing.occurredAt.isAfter(change.occurredAt)) {
            return
        }

        repository.save(
            LeaderboardExclusionProjectionEntity(
                subjectType = change.subjectType,
                subjectId = change.subjectId,
                scope = change.scope,
                active = change.active,
                occurredAt = change.occurredAt,
            ),
        )
    }
}
