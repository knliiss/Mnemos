package com.mnemos.account.persistence.repository.player

import com.mnemos.account.persistence.entity.player.PlayerIdentityRevisionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface JpaPlayerIdentityRevisionRepository : JpaRepository<PlayerIdentityRevisionEntity, UUID> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value =
            """
                INSERT INTO player_identity_revisions (
                    player_id,
                    nickname,
                    external_version,
                    observed_at,
                    updated_at
                )
                VALUES (
                    :playerId,
                    :nickname,
                    :externalVersion,
                    :observedAt,
                    :recordedAt
                )
                ON CONFLICT (player_id)
                DO UPDATE SET
                    nickname = EXCLUDED.nickname,
                    external_version = EXCLUDED.external_version,
                    observed_at = EXCLUDED.observed_at,
                    updated_at = EXCLUDED.updated_at
                WHERE player_identity_revisions.external_version < EXCLUDED.external_version
            """,
        nativeQuery = true,
    )
    fun advanceIfNewer(
        @Param("playerId") playerId: UUID,
        @Param("nickname") nickname: String,
        @Param("externalVersion") externalVersion: Long,
        @Param("observedAt") observedAt: Instant,
        @Param("recordedAt") recordedAt: Instant,
    ): Int
}
