package com.mnemos.event.application.leaderboard.exclusion.usecase

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardCacheInvalidator
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ApplyLeaderboardExclusionChangeService(
    private val repository: LeaderboardExclusionProjectionRepository,
    private val cacheInvalidator: LeaderboardCacheInvalidator,
) : ApplyLeaderboardExclusionChangeUseCase {
    override fun execute(change: LeaderboardExclusionChange) {
        if (change.subjectType != LeaderboardExclusionSubjectType.PLAYER) {
            return
        }

        repository.apply(change)

        when (change.scope) {
            LeaderboardExclusionScope.ALL -> {
                cacheInvalidator.invalidateDrops()
                cacheInvalidator.invalidateBoosters()
            }

            LeaderboardExclusionScope.DROPS -> cacheInvalidator.invalidateDrops()
            LeaderboardExclusionScope.BOOSTERS -> cacheInvalidator.invalidateBoosters()
            LeaderboardExclusionScope.COINS -> Unit
        }
    }
}
