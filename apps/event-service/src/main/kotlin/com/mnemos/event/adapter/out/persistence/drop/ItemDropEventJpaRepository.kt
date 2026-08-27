package com.mnemos.event.adapter.out.persistence.drop

import com.mnemos.event.adapter.out.persistence.drop.query.FirstItemDropProjection
import com.mnemos.event.adapter.out.persistence.drop.query.ItemDropViewProjection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface ItemDropEventJpaRepository : JpaRepository<ItemDropEventEntity, UUID> {
    @Query(
        value = """
        SELECT
            e.id,
            i.id AS item_id,
            i.name AS item_name,
            i.type AS item_type,
            i.rarity AS item_rarity,
            e.player_id,
            e.dropped_for,
            e.occurred_at
        FROM item_drop_events e
        JOIN items i ON i.id = e.item_id
        WHERE (:playerId IS NULL OR e.player_id = :playerId)
          AND (:itemName IS NULL OR :itemName <% i.name)
          AND (:itemRarity IS NULL OR i.rarity = :itemRarity)
        ORDER BY e.occurred_at DESC, e.id DESC
    """,
        countQuery = """
        SELECT count(*)
        FROM item_drop_events e
        JOIN items i ON i.id = e.item_id
        WHERE (:playerId IS NULL OR e.player_id = :playerId)
          AND (:itemName IS NULL OR :itemName <% i.name)
          AND (:itemRarity IS NULL OR i.rarity = :itemRarity)
    """,
        nativeQuery = true,
    )
    fun findViews(
        @Param("playerId") playerId: UUID?,
        @Param("itemName") itemName: String?,
        @Param("itemRarity") itemRarity: String?,
        pageable: Pageable,
    ): Page<ItemDropViewProjection>

    @Query(
        value = """
        SELECT
            e.id,
            i.id AS item_id,
            i.name AS item_name,
            i.type AS item_type,
            i.rarity AS item_rarity,
            e.player_id,
            e.dropped_for,
            e.occurred_at
        FROM item_drop_events e
        JOIN items i ON i.id = e.item_id
        WHERE e.id = :id
        """,
        nativeQuery = true,
    )
    fun findViewById(
        @Param("id") id: UUID,
    ): ItemDropViewProjection?

    @Query(
        value = """
        SELECT
            e.dropped_for AS dropped_for,
            e.occurred_at AS occurred_at
        FROM item_drop_events e
        JOIN items i ON i.id = e.item_id
        WHERE LOWER(i.name) = LOWER(:itemName)
          AND i.type = :itemType
          AND e.occurred_at >= :from
          AND e.occurred_at <= :to
        ORDER BY e.occurred_at ASC, e.id ASC
        LIMIT 1
        """,
        nativeQuery = true,
    )
    fun findFirstInRange(
        @Param("itemName") itemName: String,
        @Param("itemType") itemType: String,
        @Param("from") from: Instant,
        @Param("to") to: Instant,
    ): FirstItemDropProjection?

    @Modifying
    @Query(
        value = """
        UPDATE item_drop_events
        SET identity_resolution_pending = FALSE,
            identity_resolution_requested_at = NULL
        WHERE identity_resolution_pending = TRUE
          AND LOWER(dropped_for) = :normalizedNickname
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
        UPDATE item_drop_events
        SET player_id = :playerId,
            identity_resolution_pending = FALSE,
            identity_resolution_requested_at = NULL
        WHERE identity_resolution_pending = TRUE
          AND LOWER(dropped_for) = :normalizedNickname
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
