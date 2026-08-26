package com.mnemos.playerdirectory.application.subscription.model

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscriptionId
import java.time.Instant

data class PresenceSubscriptionView(
    val id: PresenceSubscriptionId,
    val playerId: CristalixPlayerId,
    val nickname: String,
    val presenceStatus: PlayerPresenceStatus,
    val realm: String?,
    val lastSeenOnline: Instant?,
    val createdAt: Instant,
)
