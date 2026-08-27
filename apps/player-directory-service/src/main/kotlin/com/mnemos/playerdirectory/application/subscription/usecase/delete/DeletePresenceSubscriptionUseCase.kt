package com.mnemos.playerdirectory.application.subscription.usecase.delete

import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import java.util.UUID

fun interface DeletePresenceSubscriptionUseCase {
    fun execute(
        subscriberUserId: UUID,
        playerId: CristalixPlayerId,
    )
}
