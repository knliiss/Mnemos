package com.mnemos.wallet.adapter.out.cache.leaderboard

import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardCache
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.transaction.support.TransactionSynchronizationManager

class AfterCommitCoinLeaderboardCacheInvalidatorTest {
    private val cache = mock<CoinLeaderboardCache>()
    private val invalidator = AfterCommitCoinLeaderboardCacheInvalidator(cache)

    @Test
    fun `clears immediately when no transaction is active`() {
        invalidator.invalidate()

        verify(cache).clear()
    }

    @Test
    fun `clears only after an active transaction commits`() {
        TransactionSynchronizationManager.setActualTransactionActive(true)
        TransactionSynchronizationManager.initSynchronization()

        try {
            invalidator.invalidate()

            verify(cache, never()).clear()

            TransactionSynchronizationManager
                .getSynchronizations()
                .forEach { synchronization -> synchronization.afterCommit() }

            verify(cache).clear()
        } finally {
            TransactionSynchronizationManager.clearSynchronization()
            TransactionSynchronizationManager.setActualTransactionActive(false)
        }
    }
}
