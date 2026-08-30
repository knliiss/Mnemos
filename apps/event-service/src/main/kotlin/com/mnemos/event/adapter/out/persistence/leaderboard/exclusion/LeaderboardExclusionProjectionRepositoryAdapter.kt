package com.mnemos.event.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.common.core.model.id.TimeOrderedUuidOrder
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
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

        if (existing != null && shouldIgnore(existing, change)) {
            return
        }

        repository.save(
            LeaderboardExclusionProjectionEntity(
                subjectType = change.subjectType,
                subjectId = change.subjectId,
                scope = change.scope,
                exclusionId = change.exclusionId,
                active = change.active,
                occurredAt = change.occurredAt,
            ),
        )
    }

    private fun shouldIgnore(
        existing: LeaderboardExclusionProjectionEntity,
        change: LeaderboardExclusionChange,
    ): Boolean {
        val occurredAtComparison = existing.occurredAt.compareTo(change.occurredAt)

        if (occurredAtComparison != 0) {
            return occurredAtComparison > 0
        }

        val existingExclusionId = existing.exclusionId ?: return false
        val generationComparison = TimeOrderedUuidOrder.compare(existingExclusionId, change.exclusionId)

        if (generationComparison != 0) {
            return generationComparison > 0
        }

        return !existing.active && change.active
    }
}
