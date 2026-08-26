package com.mnemos.contracts.playerdirectory.v1

import java.time.Instant
import java.util.UUID

data class PresenceSubscriptionCreatedV1(
    val subscriptionId: UUID,
    val subscriberUserId: UUID,
    val playerId: UUID,
    val occurredAt: Instant,
)
