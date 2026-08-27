package com.mnemos.playerdirectory.application.subscription.usecase.create

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.util.UUID

data class CreatePresenceSubscriptionCommand(
    val subscriberUserId: UUID,
    val playerId: CristalixPlayerId,
)
