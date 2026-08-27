package com.mnemos.playerdirectory.adapter.inbound.http.subscription

import java.util.UUID

data class CreatePresenceSubscriptionRequest(
    val playerId: UUID,
)
