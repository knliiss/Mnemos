package com.mnemos.bet.domain.eventbet.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class EventBetId(
    val id: UUID,
) {
    companion object {
        fun generate(): EventBetId = EventBetId(UuidCreator.getTimeOrderedEpoch())
    }
}
