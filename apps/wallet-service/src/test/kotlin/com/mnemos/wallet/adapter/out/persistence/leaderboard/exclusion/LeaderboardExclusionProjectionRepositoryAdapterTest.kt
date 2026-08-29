package com.mnemos.wallet.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.wallet.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Optional
import java.util.UUID

class LeaderboardExclusionProjectionRepositoryAdapterTest {
    private val repository = mock<LeaderboardExclusionProjectionJpaRepository>()
    private val adapter = LeaderboardExclusionProjectionRepositoryAdapter(repository)

    @Test
    fun `inactive coin exclusion is persisted as a tombstone`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000601")
        val id = projectionId(subjectId)
        val existing =
            projection(
                subjectId = subjectId,
                active = true,
                occurredAt = Instant.parse("2026-08-27T15:30:00Z"),
            )
        val change =
            change(
                subjectId = subjectId,
                active = false,
                occurredAt = Instant.parse("2026-08-27T15:31:00Z"),
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(existing))

        adapter.apply(change)

        val saved = argumentCaptor<LeaderboardExclusionProjectionEntity>()
        verify(repository).save(saved.capture())

        assertFalse(saved.firstValue.active)
        assertEquals(change.occurredAt, saved.firstValue.occurredAt)
    }

    @Test
    fun `stale active coin exclusion cannot replace a newer inactive tombstone`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000602")
        val id = projectionId(subjectId)
        val tombstone =
            projection(
                subjectId = subjectId,
                active = false,
                occurredAt = Instant.parse("2026-08-27T15:31:00Z"),
            )
        val staleChange =
            change(
                subjectId = subjectId,
                active = true,
                occurredAt = Instant.parse("2026-08-27T15:30:00Z"),
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(tombstone))

        adapter.apply(staleChange)

        verify(repository, never()).save(any())
    }

    private fun projectionId(subjectId: UUID): LeaderboardExclusionProjectionId =
        LeaderboardExclusionProjectionId(
            subjectType = LeaderboardExclusionSubjectType.ACCOUNT,
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.COINS,
        )

    private fun projection(
        subjectId: UUID,
        active: Boolean,
        occurredAt: Instant,
    ): LeaderboardExclusionProjectionEntity =
        LeaderboardExclusionProjectionEntity(
            subjectType = LeaderboardExclusionSubjectType.ACCOUNT,
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.COINS,
            active = active,
            occurredAt = occurredAt,
        )

    private fun change(
        subjectId: UUID,
        active: Boolean,
        occurredAt: Instant,
    ): LeaderboardExclusionChange =
        LeaderboardExclusionChange(
            subjectType = LeaderboardExclusionSubjectType.ACCOUNT,
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.COINS,
            active = active,
            occurredAt = occurredAt,
        )
}
