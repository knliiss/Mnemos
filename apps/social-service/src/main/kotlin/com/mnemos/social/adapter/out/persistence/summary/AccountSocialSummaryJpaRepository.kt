package com.mnemos.social.adapter.out.persistence.summary

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface AccountSocialSummaryJpaRepository : JpaRepository<AccountSocialSummaryEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO account_social_summaries (
                account_id,
                likes,
                dislikes,
                comments,
                revision,
                updated_at
            )
            VALUES (:accountId, 0, 0, 0, 0, :now)
            ON CONFLICT (account_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun ensureExists(
        @Param("accountId") accountId: UUID,
        @Param("now") now: Instant,
    )

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select summary from AccountSocialSummaryEntity summary where summary.accountId = :accountId")
    fun findForUpdate(
        @Param("accountId") accountId: UUID,
    ): AccountSocialSummaryEntity
}
