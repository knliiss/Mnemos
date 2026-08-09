package com.mnemos.event.domain.verification.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class EventCandidateId(
    val id: UUID,
) {
    companion object {
        fun generate(): EventCandidateId = EventCandidateId(UuidCreator.getTimeOrderedEpoch())
    }
}
