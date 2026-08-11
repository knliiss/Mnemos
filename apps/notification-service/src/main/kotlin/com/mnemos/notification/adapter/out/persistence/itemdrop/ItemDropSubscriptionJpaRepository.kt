package com.mnemos.notification.adapter.out.persistence.itemdrop

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface ItemDropSubscriptionJpaRepository : JpaRepository<ItemDropSubscriptionEntity, UUID> {
    @Query(
        """
            SELECT DISTINCT s.userId
            FROM ItemDropSubscriptionEntity ids, SubscriptionEntity s
            WHERE s.id = ids.subscriptionId
            AND (
                ids.droppedFor IS NULL
                OR LOWER(ids.droppedFor) = LOWER(:droppedFor)
            )
            AND (
                ids.itemName IS NULL
                OR LOWER(ids.itemName) = LOWER(:itemName)
            )
        """,
    )
    fun findMatchingUserIds(
        @Param("droppedFor") droppedFor: String,
        @Param("itemName") itemName: String,
    ): Set<UUID>
}
