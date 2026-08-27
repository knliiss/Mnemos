package com.mnemos.playerdirectory.adapter.out.persistence.observability

import com.mnemos.playerdirectory.adapter.out.persistence.player.PlayerEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PlayerDirectoryOperationalSnapshotJpaRepository : Repository<PlayerEntity, UUID> {
    @Query(
        value =
            """
            WITH tracked_players AS (
                SELECT tracking.player_id,
                       tracking.next_poll_at,
                       tracking.lease_until
                FROM player_tracking_state tracking
                WHERE tracking.active_until > :now
                   OR EXISTS (
                       SELECT 1
                       FROM presence_subscriptions subscription
                       WHERE subscription.player_id = tracking.player_id
                   )
            ),
            due_tracking AS (
                SELECT tracked.player_id,
                       tracked.next_poll_at
                FROM tracked_players tracked
                WHERE tracked.next_poll_at <= :now
                  AND (tracked.lease_until IS NULL OR tracked.lease_until <= :now)
            )
            SELECT
                (SELECT COUNT(*) FROM due_tracking) AS "trackingDueCount",
                (SELECT MIN(next_poll_at) FROM due_tracking) AS "oldestTrackingDueAt",
                (SELECT COUNT(*) FROM pending_player_activity) AS "resolutionQueueCount",
                (
                    SELECT COUNT(*)
                    FROM tracked_players tracked
                    JOIN players player ON player.player_id = tracked.player_id
                    WHERE player.presence_status = 'ONLINE'
                ) AS "trackedOnlineCount",
                (
                    SELECT COUNT(*)
                    FROM tracked_players tracked
                    JOIN players player ON player.player_id = tracked.player_id
                    WHERE player.presence_status = 'ONLINE'
                      AND player.privacy_hidden = FALSE
                      AND (
                          UPPER(COALESCE(player.realm_type, '')) = 'SAO'
                          OR COALESCE(player.realm, '') ~ '^SAO-[1-9][0-9]*$'
                      )
                ) AS "trackedSaoCount",
                (
                    SELECT COUNT(*)
                    FROM tracked_players tracked
                    JOIN players player ON player.player_id = tracked.player_id
                    WHERE player.presence_observed_at < :staleBefore
                ) AS "stalePresenceCount"
            """,
        nativeQuery = true,
    )
    fun readSnapshot(
        @Param("now") now: Instant,
        @Param("staleBefore") staleBefore: Instant,
    ): PlayerDirectoryOperationalSnapshotProjection
}
