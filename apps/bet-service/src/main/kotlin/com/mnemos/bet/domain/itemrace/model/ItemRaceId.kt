package com.mnemos.bet.domain.itemrace.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class ItemRaceId(
    val id: UUID,
) {
    companion object {
        fun generate(): ItemRaceId = ItemRaceId(UuidCreator.getTimeOrderedEpoch())
    }
}
