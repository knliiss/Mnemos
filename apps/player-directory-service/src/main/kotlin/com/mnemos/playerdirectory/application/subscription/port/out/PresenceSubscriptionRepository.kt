package com.mnemos.playerdirectory.application.subscription.port.out

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription
import java.util.UUID

interface PresenceSubscriptionRepository {
    fun save(subscription: PresenceSubscription): PresenceSubscription

    fun delete(subscription: PresenceSubscription)

    fun findBySubscriberUserIdAndPlayerId(
        subscriberUserId: UUID,
        playerId: CristalixPlayerId,
    ): PresenceSubscription?

    fun findAllBySubscriberUserId(subscriberUserId: UUID): List<PresenceSubscription>

    fun existsByPlayerId(playerId: CristalixPlayerId): Boolean
}
