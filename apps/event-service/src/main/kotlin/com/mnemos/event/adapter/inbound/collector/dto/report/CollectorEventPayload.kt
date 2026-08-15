package com.mnemos.event.adapter.inbound.collector.dto.report

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "kind",
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = ItemDropCollectorEventPayload::class,
        name = "ITEM_DROP",
    ),
    JsonSubTypes.Type(
        value = BoosterCollectorEventPayload::class,
        name = "BOOSTER",
    ),
    JsonSubTypes.Type(
        value = GlobalCollectorEventPayload::class,
        name = "GLOBAL",
    ),
    JsonSubTypes.Type(
        value = RaidCollectorEventPayload::class,
        name = "RAID",
    ),
)
sealed interface CollectorEventPayload
