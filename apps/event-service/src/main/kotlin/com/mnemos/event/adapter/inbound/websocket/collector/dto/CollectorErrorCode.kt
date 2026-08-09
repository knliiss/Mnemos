package com.mnemos.event.adapter.inbound.websocket.collector.dto

enum class CollectorErrorCode {
    MALFORMED_JSON,
    INVALID_MESSAGE,
    VALIDATION_FAILED,
    PROTOCOL_VIOLATION,
    INTERNAL_ERROR,
}
