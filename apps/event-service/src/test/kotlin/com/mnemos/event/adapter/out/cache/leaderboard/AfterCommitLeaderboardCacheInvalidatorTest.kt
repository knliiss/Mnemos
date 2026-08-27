package com.mnemos.event.adapter.out.cache.leaderboard

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardCache
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardCache
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.transaction.support.TransactionSynchronizationManager

class AfterCommitLeaderboardCacheInvalidatorTest {
    private val dropCache = mock<DropLeaderboardCache>()
    private val boosterCache = mock<BoosterLeaderboardCache>()
    private val invalidator =
        AfterCommitLeaderboardCacheInvalidator(
            dropCache = dropCache,
            boosterCache = boosterCache,
        )

    @Test
    fun `clears requested cache immediately when no transaction is active`() {
        invalidator.invalidateDrops()

        verify(dropCache).clear()
        verify(boosterCache, never()).clear()
    }

    @Test
    fun `clears requested cache only after an active transaction commits`() {
        TransactionSynchronizationManager.setActualTransactionActive(true)
        TransactionSynchronizationManager.initSynchronization()

        try {
            invalidator.invalidateBoosters()

            verify(boosterCache, never()).clear()

            TransactionSynchronizationManager
                .getSynchronizations()
                .forEach { synchronization -> synchronization.afterCommit() }

            verify(boosterCache).clear()
            verify(dropCache, never()).clear()
        } finally {
            TransactionSynchronizationManager.clearSynchronization()
            TransactionSynchronizationManager.setActualTransactionActive(false)
        }
    }
}
