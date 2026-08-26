package com.mnemos.notification.application.playerpresence.port.out

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.playerpresence.model.PlayerPresenceSubscription
import java.util.UUID

interface PlayerPresenceSubscriptionRepository {
    fun ensure(subscription: PlayerPresenceSubscription)

    fun delete(subscriptionId: UUID)

    fun findSubscriberUserIds(playerId: UUID): Set<UserId>
}
