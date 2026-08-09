package com.mnemos.event.adapter.inbound.websocket.collector.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = CollectorHelloMessage::class,
        name = "HELLO",
    ),
    JsonSubTypes.Type(
        value = CollectorEventReportMessage::class,
        name = "EVENT_REPORT",
    ),
)
sealed interface CollectorClientMessage
