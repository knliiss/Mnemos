package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

data class CollectorUpdateReadyMessage(
    val type: String,
    val version: String,
)
