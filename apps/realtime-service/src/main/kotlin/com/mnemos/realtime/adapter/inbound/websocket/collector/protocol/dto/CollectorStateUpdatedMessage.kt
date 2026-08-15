package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

import java.time.Instant

data class CollectorStateUpdatedMessage(
    val type: String = "COLLECTOR_STATE_UPDATED",
    val state: CollectorObservationState,
    val updatedAt: Instant,
)
