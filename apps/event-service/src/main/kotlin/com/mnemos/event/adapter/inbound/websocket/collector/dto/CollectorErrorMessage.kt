package com.mnemos.event.adapter.inbound.websocket.collector.dto

data class CollectorErrorMessage(
    val type: CollectorServerMessageType = CollectorServerMessageType.ERROR,
    val code: CollectorErrorCode,
    val message: String,
    val violations: List<CollectorFieldViolation> = emptyList(),
)
