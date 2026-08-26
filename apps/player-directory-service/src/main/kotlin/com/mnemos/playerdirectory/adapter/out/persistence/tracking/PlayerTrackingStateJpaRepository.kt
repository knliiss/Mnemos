package com.mnemos.playerdirectory.adapter.out.persistence.tracking

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PlayerTrackingStateJpaRepository : JpaRepository<PlayerTrackingStateEntity, UUID> {
    @Modifying
    @Query(
        value =
            """
            INSERT INTO player_tracking_state (
                player_id,
                active_until,
                next_poll_at,
                lease_until,
                last_poll_at,
                consecutive_failures,
                created_at,
                updated_at
            ) VALUES (
                :playerId,
                :activeUntil,
                :nextPollAt,
                NULL,
                NULL,
                0,
                :createdAt,
                :updatedAt
            )
            ON CONFLICT (player_id) DO UPDATE
            SET next_poll_at = LEAST(player_tracking_state.next_poll_at, EXCLUDED.next_poll_at),
                active_until = GREATEST(player_tracking_state.active_until, EXCLUDED.active_until),
                updated_at = GREATEST(player_tracking_state.updated_at, EXCLUDED.updated_at)
            """,
        nativeQuery = true,
    )
    fun ensureState(
        @Param("playerId") playerId: UUID,
        @Param("activeUntil") activeUntil: Instant?,
        @Param("nextPollAt") nextPollAt: Instant,
        @Param("createdAt") createdAt: Instant,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT tracking FROM PlayerTrackingStateEntity tracking WHERE tracking.playerId = :playerId")
    fun findByPlayerIdForUpdate(
        @Param("playerId") playerId: UUID,
    ): PlayerTrackingStateEntity?

    @Query(
        value =
            """
            SELECT tracking.*
            FROM player_tracking_state tracking
            WHERE tracking.next_poll_at <= :now
              AND (tracking.lease_until IS NULL OR tracking.lease_until <= :now)
              AND (
                  tracking.active_until > :now
                  OR EXISTS (
                      SELECT 1
                      FROM presence_subscriptions subscription
                      WHERE subscription.player_id = tracking.player_id
                  )
              )
            ORDER BY tracking.next_poll_at, tracking.player_id
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """,
        nativeQuery = true,
    )
    fun findDueForUpdate(
        @Param("now") now: Instant,
        @Param("limit") limit: Int,
    ): List<PlayerTrackingStateEntity>
}
