package com.mnemos.playerdirectory.domain.subscription.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.time.Instant
import java.util.UUID

data class PresenceSubscription(
    val id: PresenceSubscriptionId = PresenceSubscriptionId.generate(),
    val subscriberUserId: UUID,
    val playerId: CristalixPlayerId,
    val createdAt: Instant,
)
