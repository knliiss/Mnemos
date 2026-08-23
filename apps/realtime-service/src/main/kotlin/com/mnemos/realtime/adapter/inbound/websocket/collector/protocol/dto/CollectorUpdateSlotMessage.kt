package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

data class CollectorUpdateSlotMessage(
    val type: String = "COLLECTOR_UPDATE_SLOT",
    val granted: Boolean,
    val retryAfterSeconds: Long?,
)
