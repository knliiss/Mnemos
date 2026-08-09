package com.mnemos.event.adapter.inbound.websocket.collector.dto

data class CollectorFieldViolation(
    val field: String,
    val message: String,
)
