package com.mnemos.event.application.leaderboard.exclusion.usecase

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardCacheInvalidator
import com.mnemos.event.application.leaderboard.exclusion.port.out.LeaderboardExclusionProjectionRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class ApplyLeaderboardExclusionChangeServiceTest {
    private val repository = mock<LeaderboardExclusionProjectionRepository>()
    private val cacheInvalidator = mock<LeaderboardCacheInvalidator>()
    private val service =
        ApplyLeaderboardExclusionChangeService(
            repository = repository,
            cacheInvalidator = cacheInvalidator,
        )

    @Test
    fun `all player exclusion updates projection and invalidates both caches`() {
        val change = change(scope = LeaderboardExclusionScope.ALL)

        service.execute(change)

        verify(repository).apply(change)
        verify(cacheInvalidator).invalidateDrops()
        verify(cacheInvalidator).invalidateBoosters()
    }

    @Test
    fun `drop player exclusion invalidates only drop cache`() {
        val change = change(scope = LeaderboardExclusionScope.DROPS)

        service.execute(change)

        verify(repository).apply(change)
        verify(cacheInvalidator).invalidateDrops()
        verify(cacheInvalidator, never()).invalidateBoosters()
    }

    @Test
    fun `account exclusion is ignored by event leaderboard projection`() {
        val change =
            change(
                subjectType = LeaderboardExclusionSubjectType.ACCOUNT,
                scope = LeaderboardExclusionScope.ALL,
            )

        service.execute(change)

        verify(repository, never()).apply(change)
        verify(cacheInvalidator, never()).invalidateDrops()
        verify(cacheInvalidator, never()).invalidateBoosters()
    }

    private fun change(
        subjectType: LeaderboardExclusionSubjectType = LeaderboardExclusionSubjectType.PLAYER,
        scope: LeaderboardExclusionScope,
    ): LeaderboardExclusionChange =
        LeaderboardExclusionChange(
            exclusionId = UUID.fromString("019c1156-3274-7000-8000-000000000901"),
            subjectType = subjectType,
            subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000902"),
            scope = scope,
            active = true,
            occurredAt = Instant.parse("2026-08-30T00:30:00Z"),
        )
}
