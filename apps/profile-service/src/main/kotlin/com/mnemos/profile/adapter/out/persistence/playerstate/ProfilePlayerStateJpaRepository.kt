package com.mnemos.profile.adapter.out.persistence.playerstate

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ProfilePlayerStateJpaRepository : JpaRepository<ProfilePlayerStateEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO profile_player_states (
                player_id,
                nickname,
                external_version,
                presence_status,
                realm,
                realm_type,
                privacy_hidden,
                last_seen_online,
                presence_observed_at,
                updated_at
            ) VALUES (
                :playerId,
                :nickname,
                :externalVersion,
                :presenceStatus,
                :realm,
                :realmType,
                :privacyHidden,
                :lastSeenOnline,
                :presenceObservedAt,
                :updatedAt
            )
            ON CONFLICT (player_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("playerId") playerId: UUID,
        @Param("nickname") nickname: String,
        @Param("externalVersion") externalVersion: Long,
        @Param("presenceStatus") presenceStatus: String,
        @Param("realm") realm: String?,
        @Param("realmType") realmType: String?,
        @Param("privacyHidden") privacyHidden: Boolean,
        @Param("lastSeenOnline") lastSeenOnline: Instant?,
        @Param("presenceObservedAt") presenceObservedAt: Instant,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT state FROM ProfilePlayerStateEntity state WHERE state.playerId = :playerId")
    fun findByPlayerIdForUpdate(
        @Param("playerId") playerId: UUID,
    ): ProfilePlayerStateEntity?

    fun findAllByPlayerIdIn(playerIds: Collection<UUID>): List<ProfilePlayerStateEntity>
}
