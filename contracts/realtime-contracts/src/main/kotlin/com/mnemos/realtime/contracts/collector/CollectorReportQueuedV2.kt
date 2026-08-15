package com.mnemos.realtime.contracts.collector

import java.time.Instant
import java.util.UUID

data class CollectorReportQueuedV2(
    val ingressId: UUID,
    val collectorId: UUID,
    val rawMessage: String,
    val activeCollectors: Int,
    val receivedAt: Instant,
) {
    init {
        require(rawMessage.isNotBlank())
        require(activeCollectors > 0)
    }
}
