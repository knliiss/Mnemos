package com.mnemos.event.domain.booster.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Instant

class BoosterEvent(
    val id: EventId = EventId.generate(),
    val type: DomainBoosterType,
    val playerId: CristalixPlayerId? = null,
    val activatedBy: String,
    val activatedAt: Instant,
    val identityResolutionPending: Boolean = false,
    val identityResolutionRequestedAt: Instant? = null,
) {
    init {
        require(
            if (identityResolutionPending) {
                playerId == null && identityResolutionRequestedAt != null
            } else {
                identityResolutionRequestedAt == null
            },
        ) {
            "Booster identity resolution state is inconsistent"
        }
    }
}
