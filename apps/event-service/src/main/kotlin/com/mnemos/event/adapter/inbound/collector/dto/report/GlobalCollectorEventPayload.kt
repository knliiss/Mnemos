package com.mnemos.event.adapter.inbound.collector.dto.report

import com.mnemos.event.domain.global.model.DomainGlobalEventType

data class GlobalCollectorEventPayload(
    val eventType: DomainGlobalEventType,
) : CollectorEventPayload
