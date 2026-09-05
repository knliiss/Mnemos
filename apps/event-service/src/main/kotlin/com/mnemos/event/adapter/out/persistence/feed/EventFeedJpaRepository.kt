package com.mnemos.event.adapter.out.persistence.feed

import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
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
                    item.game_key AS item_key,
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
                item_key AS "itemKey",
                item_name AS "itemName",
                item_rarity AS "itemRarity",
                item_type AS "itemType",
                dropped_for AS "droppedFor",
                booster_type AS "boosterType",
                activated_by AS "activatedBy",
                global_type AS "globalType",
                raid_locations AS "raidLocations"
            FROM feed
            WHERE
                (
                    (event_type = 'ITEM_DROP' AND :includeItemDrop = true)
                    OR (event_type = 'BOOSTER' AND :includeBooster = true)
                    OR (event_type = 'GLOBAL' AND :includeGlobal = true)
                    OR (event_type = 'RAID' AND :includeRaid = true)
                )
                AND (CAST(:eventId AS uuid) IS NULL OR event_id = CAST(:eventId AS uuid))
                AND (CAST(:from AS timestamptz) IS NULL OR occurred_at >= CAST(:from AS timestamptz))
                AND (CAST(:to AS timestamptz) IS NULL OR occurred_at < CAST(:to AS timestamptz))
                AND (
                    CAST(:searchTerm AS text) IS NULL
                    OR position(CAST(:searchTerm AS text) in lower(coalesce(item_name, ''))) > 0
                    OR lower(item_name) % CAST(:searchTerm AS text)
                    OR position(CAST(:searchTerm AS text) in lower(coalesce(dropped_for, ''))) > 0
                    OR lower(dropped_for) % CAST(:searchTerm AS text)
                    OR position(CAST(:searchTerm AS text) in lower(coalesce(activated_by, ''))) > 0
                    OR lower(activated_by) % CAST(:searchTerm AS text)
                    OR position(CAST(:searchTerm AS text) in lower(coalesce(booster_type, ''))) > 0
                    OR position(CAST(:searchTerm AS text) in lower(coalesce(global_type, ''))) > 0
                    OR position(CAST(:searchTerm AS text) in lower(coalesce(raid_locations, ''))) > 0
                )
                AND (
                    CAST(:droppedFor AS text) IS NULL
                    OR (event_type = 'ITEM_DROP' AND lower(dropped_for) = CAST(:droppedFor AS text))
                )
                AND (
                    CAST(:itemRarity AS text) IS NULL
                    OR (event_type = 'ITEM_DROP' AND item_rarity = CAST(:itemRarity AS text))
                )
                AND (
                    CAST(:itemType AS text) IS NULL
                    OR (event_type = 'ITEM_DROP' AND item_type = CAST(:itemType AS text))
                )
                AND (
                    CAST(:boosterType AS text) IS NULL
                    OR (event_type = 'BOOSTER' AND booster_type = CAST(:boosterType AS text))
                )
                AND (
                    CAST(:globalType AS text) IS NULL
                    OR (event_type = 'GLOBAL' AND global_type = CAST(:globalType AS text))
                )
                AND (
                    CAST(:raidLocation AS integer) IS NULL
                    OR (
                        event_type = 'RAID'
                        AND CAST(:raidLocation AS text) = ANY(string_to_array(coalesce(raid_locations, ''), ','))
                    )
                )
                AND (
                    CAST(:after AS uuid) IS NULL
                    OR (:newest = true AND event_id < CAST(:after AS uuid))
                    OR (:newest = false AND event_id > CAST(:after AS uuid))
                )
            ORDER BY
                CASE WHEN :newest = true THEN event_id END DESC,
                CASE WHEN :newest = false THEN event_id END ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun read(
        @Param("after") after: UUID?,
        @Param("eventId") eventId: UUID?,
        @Param("includeItemDrop") includeItemDrop: Boolean,
        @Param("includeBooster") includeBooster: Boolean,
        @Param("includeGlobal") includeGlobal: Boolean,
        @Param("includeRaid") includeRaid: Boolean,
        @Param("searchTerm") searchTerm: String?,
        @Param("droppedFor") droppedFor: String?,
        @Param("itemRarity") itemRarity: String?,
        @Param("itemType") itemType: String?,
        @Param("boosterType") boosterType: String?,
        @Param("globalType") globalType: String?,
        @Param("raidLocation") raidLocation: Int?,
        @Param("from") from: Instant?,
        @Param("to") to: Instant?,
        @Param("newest") newest: Boolean,
        @Param("limit") limit: Int,
    ): List<EventFeedProjection>
}
