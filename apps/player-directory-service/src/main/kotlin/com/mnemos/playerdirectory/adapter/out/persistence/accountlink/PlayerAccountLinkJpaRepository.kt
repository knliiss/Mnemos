package com.mnemos.playerdirectory.adapter.out.persistence.accountlink

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PlayerAccountLinkJpaRepository : JpaRepository<PlayerAccountLinkEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO player_account_links (player_id, account_id, recorded_at)
            VALUES (:playerId, :accountId, :recordedAt)
            ON CONFLICT (player_id) DO UPDATE
            SET recorded_at = GREATEST(player_account_links.recorded_at, EXCLUDED.recorded_at)
            WHERE player_account_links.account_id = EXCLUDED.account_id
        """,
        nativeQuery = true,
    )
    fun upsert(
        @Param("playerId") playerId: UUID,
        @Param("accountId") accountId: UUID,
        @Param("recordedAt") recordedAt: Instant,
    ): Int
}
