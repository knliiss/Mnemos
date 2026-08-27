package com.mnemos.event.adapter.out.cache.leaderboard

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardCache
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardCache
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardCacheInvalidator
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Component
class AfterCommitLeaderboardCacheInvalidator(
    private val dropCache: DropLeaderboardCache,
    private val boosterCache: BoosterLeaderboardCache,
) : LeaderboardCacheInvalidator {
    override fun invalidateDrops() {
        afterCommit(dropCache::clear)
    }

    override fun invalidateBoosters() {
        afterCommit(boosterCache::clear)
    }

    private fun afterCommit(action: () -> Unit) {
        if (
            !TransactionSynchronizationManager.isActualTransactionActive() ||
            !TransactionSynchronizationManager.isSynchronizationActive()
        ) {
            action()
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            },
        )
    }
}
