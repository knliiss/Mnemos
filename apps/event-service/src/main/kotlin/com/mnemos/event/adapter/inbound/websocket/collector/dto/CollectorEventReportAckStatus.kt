package com.mnemos.event.adapter.inbound.websocket.collector.dto

enum class CollectorEventReportAckStatus {
    ACCEPTED,
    DUPLICATE,
    RECORDED,
    ALREADY_RECORDED,
}
