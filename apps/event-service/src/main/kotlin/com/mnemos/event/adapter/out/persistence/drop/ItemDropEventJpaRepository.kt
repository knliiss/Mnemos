package com.mnemos.event.adapter.out.persistence.drop

import com.mnemos.event.adapter.out.persistence.drop.query.FirstItemDropProjection
import com.mnemos.event.adapter.out.persistence.drop.query.ItemDropViewProjection
import com.mnemos.event.domain.item.model.DomainItemRarity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
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
            i.name,
            i.type,
            e.dropped_for,
            e.occurred_at
        FROM item_drop_events e
        JOIN items i ON i.id = e.item_id
        WHERE (:droppedFor IS NULL OR e.dropped_for = :droppedFor)
          AND (:itemName IS NULL OR :itemName <% i.name)
          AND (:itemRarity IS NULL OR i.rarity = :itemRarity)
        ORDER BY e.occurred_at DESC, e.id DESC
    """,
        countQuery = """
        SELECT count(*)
        FROM item_drop_events e
        JOIN items i ON i.id = e.item_id
        WHERE (:droppedFor IS NULL OR e.dropped_for = :droppedFor)
          AND (:itemName IS NULL OR :itemName <% i.name)
          AND (:itemRarity IS NULL OR i.rarity = :itemRarity)
    """,
        nativeQuery = true,
    )
    fun findViews(
        @Param("droppedFor") droppedFor: String?,
        @Param("itemName") itemName: String?,
        @Param("itemRarity") itemRarity: DomainItemRarity?,
        pageable: Pageable,
    ): Page<ItemDropViewProjection>

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
        @Param("itemName")
        itemName: String,
        @Param("itemType")
        itemType: String,
        @Param("from")
        from: Instant,
        @Param("to")
        to: Instant,
    ): FirstItemDropProjection?
}
