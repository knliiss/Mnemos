package com.mnemos.moderation.adapter.out.persistence.leaderboard

import com.mnemos.moderation.adapter.out.persistence.leaderboard.LeaderboardExclusionPersistenceMapper.toDomain
import com.mnemos.moderation.adapter.out.persistence.leaderboard.LeaderboardExclusionPersistenceMapper.toEntity
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionScope
import com.mnemos.moderation.contracts.shared.LeaderboardExclusionSubjectType
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusion
import com.mnemos.moderation.domain.leaderboard.model.LeaderboardExclusionId
import com.mnemos.moderation.domain.leaderboard.repository.LeaderboardExclusionRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class LeaderboardExclusionRepositoryAdapter(
    private val repository: LeaderboardExclusionJpaRepository,
) : LeaderboardExclusionRepository {
    override fun save(exclusion: LeaderboardExclusion): LeaderboardExclusion = repository.save(exclusion.toEntity()).toDomain()

    override fun findById(id: LeaderboardExclusionId): LeaderboardExclusion? = repository.findById(id.id).orElse(null)?.toDomain()

    override fun findBySubjectAndScope(
        subjectType: LeaderboardExclusionSubjectType,
        subjectId: UUID,
        scope: LeaderboardExclusionScope,
    ): LeaderboardExclusion? =
        repository
            .findBySubjectTypeAndSubjectIdAndScope(
                subjectType = subjectType,
                subjectId = subjectId,
                scope = scope,
            )?.toDomain()

    override fun findAll(): List<LeaderboardExclusion> = repository.findAllByOrderByCreatedAtDesc().map { entity -> entity.toDomain() }

    override fun delete(exclusion: LeaderboardExclusion) {
        repository.deleteById(exclusion.id.id)
    }
}
