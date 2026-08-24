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
              AND (:to IS NULL OR event.occurred_at < :to)
              AND (:rarity IS NULL OR item.rarity = :rarity)
              AND (:itemType IS NULL OR item.type = :itemType)
              AND (:itemName IS NULL OR lower(item.name) = lower(:itemName))
              AND (:nickname IS NULL OR position(lower(:nickname) in lower(event.dropped_for)) > 0)
            GROUP BY event.dropped_for
            ORDER BY
                CASE WHEN :sort = 'TOTAL' THEN count(*) END DESC,
                CASE WHEN :sort = 'MYTHICAL' THEN count(*) FILTER (WHERE item.rarity = 'MYTHICAL') END DESC,
                CASE WHEN :sort = 'SECRET' THEN count(*) FILTER (WHERE item.rarity = 'SECRET') END DESC,
                CASE WHEN :sort = 'RECENT' THEN max(event.occurred_at) END DESC,
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
        @Param("limit") limit: Int,
    ): List<DropLeaderboardProjection>
}
