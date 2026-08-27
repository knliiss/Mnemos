package com.mnemos.event.application.leaderboard.exclusion.usecase

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardCache
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardCache
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ApplyLeaderboardExclusionChangeService(
    private val repository: LeaderboardExclusionProjectionRepository,
    private val dropCache: DropLeaderboardCache,
    private val boosterCache: BoosterLeaderboardCache,
) : ApplyLeaderboardExclusionChangeUseCase {
    override fun execute(change: LeaderboardExclusionChange) {
        repository.apply(change)

        when (change.scope) {
            LeaderboardExclusionScope.ALL -> {
                dropCache.clear()
                boosterCache.clear()
            }

            LeaderboardExclusionScope.DROPS -> dropCache.clear()
            LeaderboardExclusionScope.BOOSTERS -> boosterCache.clear()
            LeaderboardExclusionScope.COINS -> Unit
        }
    }
}
