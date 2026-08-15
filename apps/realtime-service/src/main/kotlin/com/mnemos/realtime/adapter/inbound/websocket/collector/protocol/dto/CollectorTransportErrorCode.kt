package com.mnemos.realtime.adapter.inbound.websocket.collector.protocol.dto

enum class CollectorTransportErrorCode {
    INVALID_MESSAGE,
    MESSAGE_TOO_LARGE,
    RATE_LIMITED,
    TOO_MANY_IN_FLIGHT,
    NOT_OBSERVING,
    DELIVERY_UNAVAILABLE,
}
