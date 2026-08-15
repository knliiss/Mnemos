package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

data class CollectorTransportErrorMessage(
    val type: String = "ERROR",
    val code: CollectorTransportErrorCode,
    val message: String,
)
