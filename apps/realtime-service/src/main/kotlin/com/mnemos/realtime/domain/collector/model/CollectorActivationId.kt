package com.mnemos.realtime.domain.collector.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class CollectorActivationId(
    val id: UUID,
) {
    companion object {
        fun generate(): CollectorActivationId = CollectorActivationId(UuidCreator.getTimeOrderedEpoch())
    }
}
