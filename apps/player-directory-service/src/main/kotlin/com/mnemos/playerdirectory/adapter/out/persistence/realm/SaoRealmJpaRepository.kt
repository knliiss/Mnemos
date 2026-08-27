package com.mnemos.playerdirectory.adapter.out.persistence.realm

import com.mnemos.playerdirectory.adapter.out.persistence.player.PlayerEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.Repository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SaoRealmJpaRepository : Repository<PlayerEntity, UUID> {
    @Query(
        value =
            """
            SELECT
                player.player_id AS playerId,
                player.current_nickname AS nickname,
                link.account_id AS accountId,
                player.realm AS realm,
                player.presence_observed_at AS observedAt
            FROM players player
            LEFT JOIN player_account_links link ON link.player_id = player.player_id
            WHERE player.presence_status = 'ONLINE'
              AND player.privacy_hidden = FALSE
              AND player.realm IS NOT NULL
              AND player.presence_observed_at >= :cutoff
            ORDER BY player.presence_observed_at DESC, player.player_id
            """,
        nativeQuery = true,
    )
    fun findFreshOnlinePlayers(
        @Param("cutoff") cutoff: Instant,
    ): List<SaoRealmPlayerProjection>
}
