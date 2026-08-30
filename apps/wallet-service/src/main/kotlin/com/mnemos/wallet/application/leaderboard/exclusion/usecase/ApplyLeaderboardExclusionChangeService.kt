package com.mnemos.wallet.application.leaderboard.exclusion.usecase

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.CoinLeaderboardCacheInvalidator
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ApplyLeaderboardExclusionChangeService(
    private val repository: LeaderboardExclusionProjectionRepository,
    private val cacheInvalidator: CoinLeaderboardCacheInvalidator,
) : ApplyLeaderboardExclusionChangeUseCase {
    override fun execute(change: LeaderboardExclusionChange) {
        if (change.subjectType != LeaderboardExclusionSubjectType.ACCOUNT) {
            return
        }

        repository.apply(change)

        if (change.scope == LeaderboardExclusionScope.ALL || change.scope == LeaderboardExclusionScope.COINS) {
            cacheInvalidator.invalidate()
        }
    }
}
