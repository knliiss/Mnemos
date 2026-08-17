package com.mnemos.event.adapter.out.persistence.feed

import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface EventFeedJpaRepository : JpaRepository<ItemDropEventEntity, UUID> {
    @Query(
        value = """
            WITH feed AS (
                SELECT
                    'ITEM_DROP'::text AS event_type,
                    drop_event.id AS event_id,
                    drop_event.occurred_at AS occurred_at,
                    item.id AS item_id,
                    item.name AS item_name,
                    item.rarity AS item_rarity,
                    item.type AS item_type,
                    drop_event.dropped_for AS dropped_for,
                    NULL::text AS booster_type,
                    NULL::text AS activated_by,
                    NULL::text AS global_type,
                    NULL::text AS raid_locations
                FROM item_drop_events drop_event
                JOIN items item ON item.id = drop_event.item_id

                UNION ALL

                SELECT
                    'BOOSTER'::text,
                    booster.id,
                    booster.activated_at,
                    NULL::uuid,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    booster.type,
                    booster.activated_by,
                    NULL::text,
                    NULL::text
                FROM booster_events booster

                UNION ALL

                SELECT
                    'GLOBAL'::text,
                    global_event.id,
                    global_event.started_at,
                    NULL::uuid,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    global_event.type,
                    NULL::text
                FROM global_events global_event

                UNION ALL

                SELECT
                    'RAID'::text,
                    raid.id,
                    raid.started_at,
                    NULL::uuid,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    NULL::text,
                    string_agg(location.location::text, ',' ORDER BY location.location)
                FROM raid_events raid
                LEFT JOIN raid_event_locations location ON location.raid_event_id = raid.id
                GROUP BY raid.id, raid.started_at
            )
            SELECT
                event_type AS "eventType",
                event_id AS "eventId",
                occurred_at AS "occurredAt",
                item_id AS "itemId",
                item_name AS "itemName",
                item_rarity AS "itemRarity",
                item_type AS "itemType",
                dropped_for AS "droppedFor",
                booster_type AS "boosterType",
                activated_by AS "activatedBy",
                global_type AS "globalType",
                raid_locations AS "raidLocations"
            FROM feed
            WHERE (:after IS NULL OR event_id > :after)
            ORDER BY
                CASE WHEN :after IS NULL THEN event_id END DESC,
                CASE WHEN :after IS NOT NULL THEN event_id END ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun read(
        @Param("after") after: UUID?,
        @Param("limit") limit: Int,
    ): List<EventFeedProjection>
}
