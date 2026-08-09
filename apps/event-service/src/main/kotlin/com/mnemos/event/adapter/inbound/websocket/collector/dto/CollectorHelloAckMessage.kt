package com.mnemos.event.adapter.inbound.websocket.collector.dto

data class CollectorHelloAckMessage(
    val type: CollectorServerMessageType = CollectorServerMessageType.HELLO_ACK,
    val protocolVersion: Int,
    val collectorId: String,
    val activeCollectors: Int,
)
