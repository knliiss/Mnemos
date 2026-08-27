package com.mnemos.wallet.application.leaderboard.exclusion.usecase

import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardCache
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ApplyLeaderboardExclusionChangeService(
    private val repository: LeaderboardExclusionProjectionRepository,
    private val cache: CoinLeaderboardCache,
) : ApplyLeaderboardExclusionChangeUseCase {
    override fun execute(change: LeaderboardExclusionChange) {
        repository.apply(change)

        if (change.scope == LeaderboardExclusionScope.ALL || change.scope == LeaderboardExclusionScope.COINS) {
            cache.clear()
        }
    }
}
