package com.mnemos.realtime.contracts.collector

import java.time.Instant
import java.util.UUID

data class CollectorPresenceChangedV2(
    val changeId: UUID,
    val activeCollectors: Int,
    val revision: Long,
    val occurredAt: Instant,
) {
    init {
        require(activeCollectors >= 0)
        require(revision >= 0)
    }
}
