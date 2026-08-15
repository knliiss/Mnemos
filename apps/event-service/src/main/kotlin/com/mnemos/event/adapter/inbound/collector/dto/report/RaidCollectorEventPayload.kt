package com.mnemos.event.adapter.inbound.collector.dto.report

import jakarta.validation.constraints.Size

data class RaidCollectorEventPayload(
    @field:Size(min = 1)
    val locations: Set<Int>,
) : CollectorEventPayload
