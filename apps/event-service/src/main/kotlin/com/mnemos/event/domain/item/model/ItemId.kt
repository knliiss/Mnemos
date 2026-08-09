package com.mnemos.event.domain.item.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class ItemId(
    val id: UUID,
) {
    companion object {
        fun generate(): ItemId = ItemId(UuidCreator.getTimeOrderedEpoch())
    }
}
