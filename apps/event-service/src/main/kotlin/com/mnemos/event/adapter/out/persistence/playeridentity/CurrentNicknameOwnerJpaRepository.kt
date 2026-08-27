package com.mnemos.event.adapter.out.persistence.playeridentity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface CurrentNicknameOwnerJpaRepository : JpaRepository<CurrentNicknameOwnerEntity, String> {
    @Modifying
    @Query(
        value =
            """
            INSERT INTO current_nickname_owners (
                normalized_nickname,
                nickname,
                player_id,
                external_version,
                observed_at
            ) VALUES (
                :normalizedNickname,
                :nickname,
                :playerId,
                :externalVersion,
                :observedAt
            )
            ON CONFLICT (normalized_nickname) DO UPDATE
            SET nickname = EXCLUDED.nickname,
                player_id = EXCLUDED.player_id,
                external_version = EXCLUDED.external_version,
                observed_at = EXCLUDED.observed_at
            WHERE current_nickname_owners.observed_at < EXCLUDED.observed_at
               OR (
                    current_nickname_owners.observed_at = EXCLUDED.observed_at
                    AND current_nickname_owners.external_version <= EXCLUDED.external_version
               )
            """,
        nativeQuery = true,
    )
    fun upsertIfNotOlder(
        @Param("normalizedNickname") normalizedNickname: String,
        @Param("nickname") nickname: String,
        @Param("playerId") playerId: UUID,
        @Param("externalVersion") externalVersion: Long,
        @Param("observedAt") observedAt: Instant,
    ): Int

    @Modifying
    @Query(
        value =
            """
            DELETE FROM current_nickname_owners
            WHERE normalized_nickname = :normalizedNickname
              AND player_id = :playerId
              AND (
                    observed_at < :observedAt
                    OR (observed_at = :observedAt AND external_version <= :externalVersion)
              )
            """,
        nativeQuery = true,
    )
    fun deleteOwnedNicknameIfNotNewer(
        @Param("normalizedNickname") normalizedNickname: String,
        @Param("playerId") playerId: UUID,
        @Param("externalVersion") externalVersion: Long,
        @Param("observedAt") observedAt: Instant,
    ): Int
}
