package com.mnemos.notification.domain.playerpresence.model

import com.mnemos.common.core.model.id.UserId
import java.time.Instant
import java.util.UUID

data class PlayerPresenceSubscription(
    val subscriptionId: UUID,
    val subscriberUserId: UserId,
    val playerId: UUID,
    val createdAt: Instant,
)
