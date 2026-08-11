package com.mnemos.notification.adapter.out.persistence.subscription

import com.mnemos.notification.adapter.out.persistence.subscription.query.SubscriptionViewProjection
import com.mnemos.notification.domain.subscription.model.SubscriptionType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SubscriptionJpaRepository : JpaRepository<SubscriptionEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO subscriptions (
                id,
                user_id,
                type,
                equivalence_key,
                created_at
            )
            VALUES (
                :id,
                :userId,
                :type,
                :equivalenceKey,
                :createdAt
            )
            ON CONFLICT ON CONSTRAINT uq_subscriptions_equivalence
            DO NOTHING
        """,
        nativeQuery = true,
    )
    fun insertIfAbsent(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
        @Param("type") type: String,
        @Param("equivalenceKey") equivalenceKey: String,
        @Param("createdAt") createdAt: Instant,
    ): Int

    @Query(
        """
        SELECT s.id
        FROM SubscriptionEntity s
        WHERE s.userId = :userId
          AND s.type = :type
          AND s.equivalenceKey = :equivalenceKey
        """,
    )
    fun findEquivalentId(
        @Param("userId") userId: UUID,
        @Param("type") type: SubscriptionType,
        @Param("equivalenceKey") equivalenceKey: String,
    ): UUID?

    @Modifying
    @Query(
        value = """
            DELETE FROM subscriptions
            WHERE id = :id
              AND user_id = :userId
        """,
        nativeQuery = true,
    )
    fun deleteOwnedById(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
    ): Int

    @Query(
        value = """
            SELECT
                id,
                type,
                created_at AS "createdAt",
                dropped_for AS "droppedFor",
                item_name AS "itemName",
                booster_type AS "boosterType",
                dungeon_type AS "dungeonType",
                global_event_type AS "globalEventType",
                raid_locations AS "raidLocations"
            FROM subscription_views
            WHERE user_id = :userId
            ORDER BY created_at DESC, id DESC
        """,
        countQuery = """
            SELECT count(*)
            FROM subscriptions
            WHERE user_id = :userId
        """,
        nativeQuery = true,
    )
    fun findViews(
        @Param("userId") userId: UUID,
        pageable: Pageable,
    ): Page<SubscriptionViewProjection>
}
