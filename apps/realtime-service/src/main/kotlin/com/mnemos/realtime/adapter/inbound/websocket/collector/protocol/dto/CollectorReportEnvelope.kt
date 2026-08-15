package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class CollectorReportEnvelope(
    val type: String,
    val messageId: UUID,
)
