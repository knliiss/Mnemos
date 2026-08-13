package com.mnemos.bet.domain.eventbet.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class EventBetRoundId(
    val id: UUID,
) {
    companion object {
        fun generate(): EventBetRoundId = EventBetRoundId(UuidCreator.getTimeOrderedEpoch())
    }
}
