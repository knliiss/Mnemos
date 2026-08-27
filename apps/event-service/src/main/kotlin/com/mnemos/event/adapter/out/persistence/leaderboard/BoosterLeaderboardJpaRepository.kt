package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.adapter.out.persistence.booster.BoosterEventEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface BoosterLeaderboardJpaRepository : Repository<BoosterEventEntity, UUID> {
    @Query(
        value = """
            SELECT
                event.player_id AS "playerId",
                identity.nickname AS "nickname",
                count(*) AS "totalBoosters",
                max(event.activated_at) AS "lastActivatedAt"
            FROM booster_events event
            JOIN player_identity_projection identity ON identity.player_id = event.player_id
            WHERE event.player_id IS NOT NULL
              AND event.activated_at >= :from
              AND (CAST(:to AS timestamptz) IS NULL OR event.activated_at < CAST(:to AS timestamptz))
              AND NOT EXISTS (
                  SELECT 1
                  FROM leaderboard_exclusion_projection exclusion
                  WHERE exclusion.subject_type = 'PLAYER'
                    AND exclusion.subject_id = event.player_id
                    AND exclusion.scope IN ('ALL', 'BOOSTERS')
              )
            GROUP BY event.player_id, identity.nickname
            ORDER BY count(*) DESC, max(event.activated_at) DESC, lower(identity.nickname) ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun read(
        @Param("from") from: Instant,
        @Param("to") to: Instant?,
        @Param("limit") limit: Int,
    ): List<BoosterLeaderboardProjection>
}
