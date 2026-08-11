package com.mnemos.notification.adapter.out.persistence.booster

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface BoosterSubscriptionJpaRepository : JpaRepository<BoosterSubscriptionEntity, UUID> {
    @Query(
        """
            SELECT DISTINCT s.userId
            FROM BoosterSubscriptionEntity bs, SubscriptionEntity s
            WHERE s.id = bs.subscriptionId
                AND bs.boosterType = :boosterType
        """,
    )
    fun findMatchingUserIds(
        @Param("boosterType") boosterType: DomainBoosterType,
    ): Set<UUID>
}
