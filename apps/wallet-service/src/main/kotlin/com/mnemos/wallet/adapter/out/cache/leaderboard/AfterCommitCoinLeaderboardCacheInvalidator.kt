package com.mnemos.wallet.adapter.out.cache.leaderboard

import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardCache
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.CoinLeaderboardCacheInvalidator
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class AfterCommitCoinLeaderboardCacheInvalidator(
    private val cache: CoinLeaderboardCache,
) : CoinLeaderboardCacheInvalidator {
    override fun invalidate() {
        if (
            !TransactionSynchronizationManager.isActualTransactionActive() ||
            !TransactionSynchronizationManager.isSynchronizationActive()
        ) {
            cache.clear()
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    cache.clear()
                }
            },
        )
    }
}
