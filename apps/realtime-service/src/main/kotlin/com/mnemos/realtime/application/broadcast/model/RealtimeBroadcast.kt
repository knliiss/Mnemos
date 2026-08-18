package com.mnemos.realtime.application.broadcast.model

import java.time.Instant
import java.util.UUID

data class RealtimeBroadcast(
    val id: UUID,
    val sequence: Long,
    val occurredAt: Instant,
    val expiresAt: Instant,
    val payload: DropSpotlightBroadcastPayload?,
    val type: RealtimeBroadcastType = RealtimeBroadcastType.DROP_SPOTLIGHT,
    val gameEventPayload: GameEventBroadcastPayload? = null,
) {
    init {
        when (type) {
            RealtimeBroadcastType.DROP_SPOTLIGHT ->
                require(payload != null && gameEventPayload == null) {
                    "Drop spotlight broadcast must contain only a spotlight payload"
                }

            RealtimeBroadcastType.GAME_EVENT ->
                require(payload == null && gameEventPayload != null) {
                    "Game event broadcast must contain only a game event payload"
                }
        }
    }
}
