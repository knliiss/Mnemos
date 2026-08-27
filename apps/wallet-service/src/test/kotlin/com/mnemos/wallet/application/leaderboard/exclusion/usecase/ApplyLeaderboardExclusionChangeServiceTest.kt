package com.mnemos.wallet.application.leaderboard.exclusion.usecase

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.CoinLeaderboardCacheInvalidator
import com.mnemos.wallet.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class ApplyLeaderboardExclusionChangeServiceTest {
    private val repository = mock<LeaderboardExclusionProjectionRepository>()
    private val cacheInvalidator = mock<CoinLeaderboardCacheInvalidator>()
    private val service =
        ApplyLeaderboardExclusionChangeService(
            repository = repository,
            cacheInvalidator = cacheInvalidator,
        )

    @Test
    fun `coin exclusion updates projection and invalidates cache`() {
        val change = change(LeaderboardExclusionScope.COINS)

        service.execute(change)

        verify(repository).apply(change)
        verify(cacheInvalidator).invalidate()
    }

    @Test
    fun `all exclusion updates projection and invalidates cache`() {
        val change = change(LeaderboardExclusionScope.ALL)

        service.execute(change)

        verify(repository).apply(change)
        verify(cacheInvalidator).invalidate()
    }

    @Test
    fun `unrelated exclusion does not invalidate coin cache`() {
        val change = change(LeaderboardExclusionScope.DROPS)

        service.execute(change)

        verify(repository).apply(change)
        verify(cacheInvalidator, never()).invalidate()
    }

    private fun change(scope: LeaderboardExclusionScope): LeaderboardExclusionChange =
        LeaderboardExclusionChange(
            subjectType = LeaderboardExclusionSubjectType.ACCOUNT,
            subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000501"),
            scope = scope,
            active = true,
            occurredAt = Instant.parse("2026-08-27T15:30:00Z"),
        )
}
