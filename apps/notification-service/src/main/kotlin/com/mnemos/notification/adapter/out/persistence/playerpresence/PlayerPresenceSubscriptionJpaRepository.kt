package com.mnemos.notification.adapter.out.persistence.playerpresence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface PlayerPresenceSubscriptionJpaRepository : JpaRepository<PlayerPresenceSubscriptionEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO player_presence_subscriptions (
                subscription_id,
                subscriber_user_id,
                player_id,
                created_at
            ) VALUES (
                :subscriptionId,
                :subscriberUserId,
                :playerId,
                :createdAt
            )
            ON CONFLICT (subscription_id) DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("subscriptionId") subscriptionId: UUID,
        @Param("subscriberUserId") subscriberUserId: UUID,
        @Param("playerId") playerId: UUID,
        @Param("createdAt") createdAt: Instant,
    ): Int

    @Query(
        """
        SELECT subscription.subscriberUserId
        FROM PlayerPresenceSubscriptionEntity subscription
        WHERE subscription.playerId = :playerId
        """,
    )
    fun findSubscriberUserIdsByPlayerId(
        @Param("playerId") playerId: UUID,
    ): List<UUID>
}
