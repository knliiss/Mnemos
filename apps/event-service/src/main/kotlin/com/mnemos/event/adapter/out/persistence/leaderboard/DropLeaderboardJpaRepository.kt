package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface DropLeaderboardJpaRepository : Repository<ItemDropEventEntity, UUID> {
    @Query(
        value = """
            SELECT
                event.dropped_for AS "nickname",
                count(*) AS "totalDrops",
                count(*) FILTER (WHERE item.rarity = 'MYTHICAL') AS "mythicalDrops",
                count(*) FILTER (WHERE item.rarity = 'SECRET') AS "secretDrops",
                max(event.occurred_at) AS "lastDropAt"
            FROM item_drop_events event
            JOIN items item ON item.id = event.item_id
            WHERE event.occurred_at >= :from
              AND (CAST(:to AS timestamptz) IS NULL OR event.occurred_at < CAST(:to AS timestamptz))
              AND (CAST(:rarity AS text) IS NULL OR item.rarity = CAST(:rarity AS text))
              AND (CAST(:itemType AS text) IS NULL OR item.type = CAST(:itemType AS text))
              AND (CAST(:itemName AS text) IS NULL OR lower(item.name) = lower(CAST(:itemName AS text)))
              AND (
                  CAST(:nickname AS text) IS NULL
                  OR position(lower(CAST(:nickname AS text)) in lower(event.dropped_for)) > 0
              )
            GROUP BY event.dropped_for
            ORDER BY
                CASE WHEN :sort = 'TOTAL' AND :direction = 'DESC' THEN count(*) END DESC,
                CASE WHEN :sort = 'TOTAL' AND :direction = 'ASC' THEN count(*) END ASC,
                CASE WHEN :sort = 'MYTHICAL' AND :direction = 'DESC' THEN
                    count(*) FILTER (WHERE item.rarity = 'MYTHICAL')
                END DESC,
                CASE WHEN :sort = 'MYTHICAL' AND :direction = 'ASC' THEN
                    count(*) FILTER (WHERE item.rarity = 'MYTHICAL')
                END ASC,
                CASE WHEN :sort = 'SECRET' AND :direction = 'DESC' THEN
                    count(*) FILTER (WHERE item.rarity = 'SECRET')
                END DESC,
                CASE WHEN :sort = 'SECRET' AND :direction = 'ASC' THEN
                    count(*) FILTER (WHERE item.rarity = 'SECRET')
                END ASC,
                count(*) DESC,
                lower(event.dropped_for) ASC
            LIMIT :limit
        """,
        nativeQuery = true,
    )
    fun read(
        @Param("from") from: Instant,
        @Param("to") to: Instant?,
        @Param("rarity") rarity: String?,
        @Param("itemType") itemType: String?,
        @Param("itemName") itemName: String?,
        @Param("nickname") nickname: String?,
        @Param("sort") sort: String,
        @Param("direction") direction: String,
        @Param("limit") limit: Int,
    ): List<DropLeaderboardProjection>
}
