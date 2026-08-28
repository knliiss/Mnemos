package com.mnemos.profile.adapter.out.persistence.profile

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PendingSocialSummaryJpaRepository : JpaRepository<PendingSocialSummaryEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO pending_social_summaries (
                account_id,
                likes,
                dislikes,
                comments,
                revision,
                occurred_at
            )
            VALUES (
                :accountId,
                :likes,
                :dislikes,
                :comments,
                :revision,
                :occurredAt
            )
            ON CONFLICT (account_id) DO UPDATE
            SET likes = EXCLUDED.likes,
                dislikes = EXCLUDED.dislikes,
                comments = EXCLUDED.comments,
                revision = EXCLUDED.revision,
                occurred_at = EXCLUDED.occurred_at
            WHERE EXCLUDED.revision > pending_social_summaries.revision
        """,
        nativeQuery = true,
    )
    fun saveIfNewer(
        @Param("accountId") accountId: UUID,
        @Param("likes") likes: Long,
        @Param("dislikes") dislikes: Long,
        @Param("comments") comments: Long,
        @Param("revision") revision: Long,
        @Param("occurredAt") occurredAt: Instant,
    ): Int

    @Query(
        value = """
            SELECT pending.*
            FROM pending_social_summaries pending
            WHERE EXISTS (
                SELECT 1
                FROM public_profiles profile
                WHERE profile.account_id = pending.account_id
            )
            ORDER BY pending.occurred_at ASC, pending.account_id ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun findReady(
        @Param("limit") limit: Int,
    ): List<PendingSocialSummaryEntity>

    @Modifying
    @Query(
        value = """
            DELETE FROM pending_social_summaries
            WHERE account_id = :accountId
              AND revision = :revision
        """,
        nativeQuery = true,
    )
    fun deleteIfRevision(
        @Param("accountId") accountId: UUID,
        @Param("revision") revision: Long,
    ): Int
}
