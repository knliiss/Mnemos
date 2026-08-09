package com.mnemos.event.adapter.inbound.http.global

import com.mnemos.event.domain.global.model.DomainGlobalEventType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ListGlobalEventRequest(
    val types: List<DomainGlobalEventType> = emptyList(),
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
