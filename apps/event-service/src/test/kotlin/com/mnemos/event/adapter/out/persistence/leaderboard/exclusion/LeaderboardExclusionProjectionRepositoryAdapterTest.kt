package com.mnemos.event.adapter.out.persistence.leaderboard.exclusion

import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionChange
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionScope
import com.mnemos.event.application.leaderboard.exclusion.model.LeaderboardExclusionSubjectType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
    private val firstGenerationId = UUID.fromString("019c1156-3274-7000-8000-000000000701")
    private val secondGenerationId = UUID.fromString("019c1156-3275-7000-8000-000000000701")

    @Test
    fun `inactive player exclusion is persisted as a tombstone`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000702")
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

        assertEquals(firstGenerationId, saved.firstValue.exclusionId)
        assertFalse(saved.firstValue.active)
        assertEquals(change.occurredAt, saved.firstValue.occurredAt)
    }

    @Test
    fun `stale active player exclusion cannot replace a newer inactive tombstone`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000703")
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

    @Test
    fun `newer generation can recreate exclusion at the same timestamp`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000704")
        val id = projectionId(subjectId)
        val occurredAt = Instant.parse("2026-08-27T15:31:00Z")
        val tombstone =
            projection(
                subjectId = subjectId,
                exclusionId = firstGenerationId,
                active = false,
                occurredAt = occurredAt,
            )
        val recreated =
            change(
                subjectId = subjectId,
                exclusionId = secondGenerationId,
                active = true,
                occurredAt = occurredAt,
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(tombstone))

        adapter.apply(recreated)

        val saved = argumentCaptor<LeaderboardExclusionProjectionEntity>()
        verify(repository).save(saved.capture())

        assertEquals(secondGenerationId, saved.firstValue.exclusionId)
        assertTrue(saved.firstValue.active)
    }

    @Test
    fun `older generation cannot replace a newer generation at the same timestamp`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000705")
        val id = projectionId(subjectId)
        val occurredAt = Instant.parse("2026-08-27T15:31:00Z")
        val existing =
            projection(
                subjectId = subjectId,
                exclusionId = secondGenerationId,
                active = true,
                occurredAt = occurredAt,
            )
        val staleDelete =
            change(
                subjectId = subjectId,
                exclusionId = firstGenerationId,
                active = false,
                occurredAt = occurredAt,
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(existing))

        adapter.apply(staleDelete)

        verify(repository, never()).save(any())
    }

    @Test
    fun `inactive state wins an active replay within the same generation and timestamp`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000706")
        val id = projectionId(subjectId)
        val occurredAt = Instant.parse("2026-08-27T15:31:00Z")
        val tombstone =
            projection(
                subjectId = subjectId,
                active = false,
                occurredAt = occurredAt,
            )
        val replay =
            change(
                subjectId = subjectId,
                active = true,
                occurredAt = occurredAt,
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(tombstone))

        adapter.apply(replay)

        verify(repository, never()).save(any())
    }

    @Test
    fun `legacy projection without generation accepts an equal timestamp change`() {
        val subjectId = UUID.fromString("019c1156-3274-7000-8000-000000000707")
        val id = projectionId(subjectId)
        val occurredAt = Instant.parse("2026-08-27T15:31:00Z")
        val legacyProjection =
            projection(
                subjectId = subjectId,
                exclusionId = null,
                active = true,
                occurredAt = occurredAt,
            )
        val change =
            change(
                subjectId = subjectId,
                active = false,
                occurredAt = occurredAt,
            )

        whenever(repository.findById(id)).thenReturn(Optional.of(legacyProjection))

        adapter.apply(change)

        verify(repository).save(any())
    }

    private fun projectionId(subjectId: UUID): LeaderboardExclusionProjectionId =
        LeaderboardExclusionProjectionId(
            subjectType = LeaderboardExclusionSubjectType.PLAYER,
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.DROPS,
        )

    private fun projection(
        subjectId: UUID,
        exclusionId: UUID? = firstGenerationId,
        active: Boolean,
        occurredAt: Instant,
    ): LeaderboardExclusionProjectionEntity =
        LeaderboardExclusionProjectionEntity(
            subjectType = LeaderboardExclusionSubjectType.PLAYER,
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.DROPS,
            exclusionId = exclusionId,
            active = active,
            occurredAt = occurredAt,
        )

    private fun change(
        subjectId: UUID,
        exclusionId: UUID = firstGenerationId,
        active: Boolean,
        occurredAt: Instant,
    ): LeaderboardExclusionChange =
        LeaderboardExclusionChange(
            exclusionId = exclusionId,
            subjectType = LeaderboardExclusionSubjectType.PLAYER,
            subjectId = subjectId,
            scope = LeaderboardExclusionScope.DROPS,
            active = active,
            occurredAt = occurredAt,
        )
}
