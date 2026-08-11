package com.mnemos.notification.adapter.out.persistence.global

import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface GlobalSubscriptionJpaRepository : JpaRepository<GlobalSubscriptionEntity, UUID> {
    @Query(
        """
            SELECT DISTINCT s.userId
            FROM GlobalSubscriptionEntity gs, SubscriptionEntity s
            WHERE gs.subscriptionId = s.id
                AND (gs.eventType = :eventType)
        """,
    )
    fun findMatchingUserIds(
        @Param("eventType") eventType: DomainGlobalEventType,
    ): Set<UUID>
}
