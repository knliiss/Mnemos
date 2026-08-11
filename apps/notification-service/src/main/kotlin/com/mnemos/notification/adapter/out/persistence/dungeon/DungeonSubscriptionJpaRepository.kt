package com.mnemos.notification.adapter.out.persistence.dungeon

import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface DungeonSubscriptionJpaRepository : JpaRepository<DungeonSubscriptionEntity, UUID> {
    @Query(
        """
            SELECT DISTINCT s.userId
            FROM DungeonSubscriptionEntity ds, SubscriptionEntity s
            WHERE ds.subscriptionId = s.id
                AND ds.dungeonType = :dungeonType
        """,
    )
    fun findMatchingUserIds(
        @Param("dungeonType") dungeonType: DomainDungeonType,
    ): Set<UUID>
}
