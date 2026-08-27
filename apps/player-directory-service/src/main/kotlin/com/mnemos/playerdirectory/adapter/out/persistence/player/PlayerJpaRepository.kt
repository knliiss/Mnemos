package com.mnemos.playerdirectory.adapter.out.persistence.player

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PlayerJpaRepository : JpaRepository<PlayerEntity, UUID> {
    @Modifying
    @Query(
        value =
            """
            INSERT INTO players (
                player_id,
                current_nickname,
                normalized_nickname,
                external_version,
                external_updated_at,
                presence_status,
                last_verified_presence_status,
                realm,
                realm_type,
                privacy_hidden,
                last_seen_online,
                presence_observed_at,
                offline_candidate_since,
                offline_candidate_count,
                created_at,
                updated_at
            ) VALUES (
                :playerId,
                :currentNickname,
                :normalizedNickname,
                :externalVersion,
                :externalUpdatedAt,
                :presenceStatus,
                :lastVerifiedPresenceStatus,
                :realm,
                :realmType,
                :privacyHidden,
                :lastSeenOnline,
                :presenceObservedAt,
                :offlineCandidateSince,
                :offlineCandidateCount,
                :createdAt,
                :updatedAt
            )
            ON CONFLICT (player_id) DO NOTHING
            """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("playerId") playerId: UUID,
        @Param("currentNickname") currentNickname: String,
        @Param("normalizedNickname") normalizedNickname: String,
        @Param("externalVersion") externalVersion: Long,
        @Param("externalUpdatedAt") externalUpdatedAt: Instant?,
        @Param("presenceStatus") presenceStatus: String,
        @Param("lastVerifiedPresenceStatus") lastVerifiedPresenceStatus: String?,
        @Param("realm") realm: String?,
        @Param("realmType") realmType: String?,
        @Param("privacyHidden") privacyHidden: Boolean,
        @Param("lastSeenOnline") lastSeenOnline: Instant?,
        @Param("presenceObservedAt") presenceObservedAt: Instant,
        @Param("offlineCandidateSince") offlineCandidateSince: Instant?,
        @Param("offlineCandidateCount") offlineCandidateCount: Int,
        @Param("createdAt") createdAt: Instant,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT player FROM PlayerEntity player WHERE player.playerId = :playerId")
    fun findByPlayerIdForUpdate(
        @Param("playerId") playerId: UUID,
    ): PlayerEntity?
}
