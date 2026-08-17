package com.mnemos.profile.adapter.out.persistence.profile

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface PendingSocialSummaryJpaRepository : JpaRepository<PendingSocialSummaryEntity, UUID> {
    @Query(
        value = """
            SELECT pending.*
            FROM pending_social_summaries pending
            WHERE EXISTS (
                SELECT 1
                FROM public_profiles profile
                WHERE profile.account_id = pending.account_id
            )
            ORDER BY pending.occurred_at ASC
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
