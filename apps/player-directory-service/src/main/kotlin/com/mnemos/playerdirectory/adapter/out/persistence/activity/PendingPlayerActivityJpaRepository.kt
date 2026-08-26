package com.mnemos.playerdirectory.adapter.out.persistence.activity

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface PendingPlayerActivityJpaRepository : JpaRepository<PendingPlayerActivityEntity, String> {
    @Modifying
    @Query(
        value =
            """
            INSERT INTO pending_player_activity (
                normalized_nickname,
                nickname,
                pending_drop_count,
                last_drop_at,
                next_attempt_at,
                lease_until,
                consecutive_failures,
                created_at,
                updated_at
            ) VALUES (
                :normalizedNickname,
                :nickname,
                1,
                :occurredAt,
                :recordedAt,
                NULL,
                0,
                :recordedAt,
                :recordedAt
            )
            ON CONFLICT (normalized_nickname) DO UPDATE
            SET nickname = EXCLUDED.nickname,
                pending_drop_count = pending_player_activity.pending_drop_count + 1,
                last_drop_at = GREATEST(pending_player_activity.last_drop_at, EXCLUDED.last_drop_at),
                next_attempt_at = LEAST(pending_player_activity.next_attempt_at, EXCLUDED.next_attempt_at),
                updated_at = GREATEST(pending_player_activity.updated_at, EXCLUDED.updated_at)
            """,
        nativeQuery = true,
    )
    fun enqueueDrop(
        @Param("normalizedNickname") normalizedNickname: String,
        @Param("nickname") nickname: String,
        @Param("occurredAt") occurredAt: Instant,
        @Param("recordedAt") recordedAt: Instant,
    ): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT activity FROM PendingPlayerActivityEntity activity " +
            "WHERE activity.normalizedNickname = :normalizedNickname"
    )
    fun findByNormalizedNicknameForUpdate(
        @Param("normalizedNickname") normalizedNickname: String,
    ): PendingPlayerActivityEntity?

    @Query(
        value =
            """
            SELECT activity.*
            FROM pending_player_activity activity
            WHERE activity.next_attempt_at <= :now
              AND (activity.lease_until IS NULL OR activity.lease_until <= :now)
            ORDER BY activity.next_attempt_at, activity.normalized_nickname
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """,
        nativeQuery = true,
    )
    fun findDueForUpdate(
        @Param("now") now: Instant,
        @Param("limit") limit: Int,
    ): List<PendingPlayerActivityEntity>
}
