package com.mnemos.event.domain.drop.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.ItemId
import java.time.Instant

class ItemDropEvent(
    val id: EventId = EventId.generate(),
    val itemId: ItemId,
    val droppedFor: String,
    val occurredAt: Instant,
)
