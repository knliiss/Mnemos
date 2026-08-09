package com.mnemos.event.adapter.inbound.websocket.collector.dto

import com.mnemos.event.adapter.inbound.websocket.collector.dto.report.CollectorEventPayload
import jakarta.validation.Valid
import java.time.Instant
import java.util.UUID

data class CollectorEventReportMessage(
    val messageId: UUID,
    val observedAt: Instant,
    @field:Valid
    val event: CollectorEventPayload,
) : CollectorClientMessage
