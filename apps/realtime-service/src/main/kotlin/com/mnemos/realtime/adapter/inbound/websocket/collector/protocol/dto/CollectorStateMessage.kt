package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CollectorStateMessage(
    val type: String,
    val state: CollectorObservationState,
)
