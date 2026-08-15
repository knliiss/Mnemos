package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

import java.time.Instant
import java.util.UUID

data class CollectorReportQueuedMessage(
    val type: String = "REPORT_QUEUED",
    val messageId: UUID,
    val queuedAt: Instant,
)
