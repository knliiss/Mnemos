package com.mnemos.event.domain

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class EventId(
    val id: UUID,
) {
    companion object {
        fun generate() = EventId(UuidCreator.getTimeOrderedEpoch())
    }
}
