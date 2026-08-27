package com.mnemos.event.domain.drop.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.ItemId
import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Instant

class ItemDropEvent(
    val id: EventId = EventId.generate(),
    val itemId: ItemId,
    val playerId: CristalixPlayerId? = null,
    val droppedFor: String,
    val occurredAt: Instant,
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
            "Item drop identity resolution state is inconsistent"
        }
    }
}
