package com.mnemos.notification.adapter.out.persistence.raid

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RaidSubscriptionJpaRepository : JpaRepository<RaidSubscriptionEntity, UUID> {
    @Query(
        """
            SELECT DISTINCT s.user_id
            FROM raid_subscriptions rs
            JOIN subscriptions s
                ON rs.subscription_id = s.id
            WHERE NOT EXISTS (
                SELECT 1
                FROM raid_subscription_locations rsl
                WHERE rsl.subscription_id = rs.id
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
