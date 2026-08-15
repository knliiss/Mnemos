package com.mnemos.event.adapter.inbound.collector.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = CollectorEventReportMessage::class,
        name = "EVENT_REPORT",
    ),
)
sealed interface CollectorClientMessage
