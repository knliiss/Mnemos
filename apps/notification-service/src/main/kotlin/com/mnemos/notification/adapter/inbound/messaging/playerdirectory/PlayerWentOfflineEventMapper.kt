package com.mnemos.notification.adapter.inbound.messaging.playerdirectory

import com.mnemos.contracts.playerdirectory.v1.PlayerWentOfflineV1
import com.mnemos.notification.application.playerpresence.usecase.process.ProcessPlayerWentOfflineEventCommand
import java.util.UUID

object PlayerWentOfflineEventMapper {
    fun PlayerWentOfflineV1.toCommand(messageId: UUID): ProcessPlayerWentOfflineEventCommand =
        ProcessPlayerWentOfflineEventCommand(
            messageId = messageId,
            playerId = playerId,
            nickname = nickname,
            previousRealm = previousRealm,
            lastSeenOnline = lastSeenOnline,
            occurredAt = observedAt,
        )
}
