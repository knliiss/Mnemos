package com.mnemos.notification.adapter.out.persistence.raid

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RaidSubscriptionJpaRepository : JpaRepository<RaidSubscriptionEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
            SELECT subscription
            FROM RaidSubscriptionEntity subscription
            WHERE subscription.subscriptionId = :subscriptionId
        """,
    )
    fun findBySubscriptionIdForUpdate(
        @Param("subscriptionId") subscriptionId: UUID,
    ): RaidSubscriptionEntity?

    @Query(
        """
            SELECT DISTINCT s.user_id
            FROM raid_subscriptions rs
            JOIN subscriptions s
                ON rs.subscription_id = s.id
            WHERE NOT EXISTS (
                SELECT 1
                FROM raid_subscription_locations rsl
                WHERE rsl.subscription_id = rs.subscription_id
            )
        """,
        nativeQuery = true,
    )
    fun findAllLocationsSubscriberUserIds(): Set<UUID>

    @Query(
        """
            SELECT DISTINCT s.user_id
            FROM raid_subscriptions rs
            JOIN subscriptions s
                ON rs.subscription_id = s.id
            JOIN raid_subscription_locations rsl
                ON rsl.subscription_id = rs.subscription_id
            WHERE rsl.location IN (:locations)
        """,
        nativeQuery = true,
    )
    fun findSubscriberUserIdsByLocations(
        @Param("locations") locations: Set<Int>,
    ): Set<UUID>
}
