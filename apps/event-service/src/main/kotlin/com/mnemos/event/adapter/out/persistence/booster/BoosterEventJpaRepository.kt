package com.mnemos.event.adapter.out.persistence.booster

import com.mnemos.event.adapter.out.persistence.booster.query.BoosterEventViewProjection
import com.mnemos.event.domain.booster.model.DomainBoosterType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface BoosterEventJpaRepository : JpaRepository<BoosterEventEntity, UUID> {
    @Query(
        """
            SELECT
                e.id,
                e.type,
                e.playerId,
                e.activatedBy,
                e.activatedAt
            FROM BoosterEventEntity e
            WHERE (:type IS NULL OR e.type = :type)
              AND (:playerId IS NULL OR e.playerId = :playerId)
            ORDER BY e.activatedAt DESC, e.id DESC
        """,
    )
    fun findViews(
        @Param("type") type: DomainBoosterType?,
        @Param("playerId") playerId: UUID?,
        pageable: Pageable,
    ): Page<BoosterEventViewProjection>

    @Modifying
    @Query(
        value = """
        UPDATE booster_events
        SET identity_resolution_pending = FALSE,
            identity_resolution_requested_at = NULL
        WHERE identity_resolution_pending = TRUE
          AND LOWER(activated_by) = :normalizedNickname
          AND identity_resolution_requested_at < :cutoff
        """,
        nativeQuery = true,
    )
    fun expirePendingIdentity(
        @Param("normalizedNickname") normalizedNickname: String,
        @Param("cutoff") cutoff: Instant,
    ): Int

    @Modifying
    @Query(
        value = """
        UPDATE booster_events
        SET player_id = :playerId,
            identity_resolution_pending = FALSE,
            identity_resolution_requested_at = NULL
        WHERE identity_resolution_pending = TRUE
          AND LOWER(activated_by) = :normalizedNickname
          AND identity_resolution_requested_at >= :cutoff
        """,
        nativeQuery = true,
    )
    fun resolvePendingIdentity(
        @Param("normalizedNickname") normalizedNickname: String,
        @Param("playerId") playerId: UUID,
        @Param("cutoff") cutoff: Instant,
    ): Int
}
