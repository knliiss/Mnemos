package com.mnemos.event.adapter.inbound.websocket.collector.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern

data class CollectorHelloMessage(
    @field:Min(1)
    val protocolVersion: Int,
    @field:Pattern(regexp = "^[0-9a-f]{64}$")
    val collectorId: String,
) : CollectorClientMessage
