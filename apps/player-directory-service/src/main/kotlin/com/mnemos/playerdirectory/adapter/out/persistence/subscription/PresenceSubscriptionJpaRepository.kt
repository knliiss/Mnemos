package com.mnemos.playerdirectory.adapter.out.persistence.subscription

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PresenceSubscriptionJpaRepository : JpaRepository<PresenceSubscriptionEntity, UUID> {
    fun findBySubscriberUserIdAndPlayerId(
        subscriberUserId: UUID,
        playerId: UUID,
    ): PresenceSubscriptionEntity?

    fun findAllBySubscriberUserIdOrderByCreatedAtDesc(subscriberUserId: UUID): List<PresenceSubscriptionEntity>

    fun existsByPlayerId(playerId: UUID): Boolean
}
