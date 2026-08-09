package com.mnemos.event.adapter.inbound.http.raid

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.hibernate.validator.constraints.Range

data class ListRaidEventRequest(
    @field:Range(min = 1, max = 30)
    val minLocation: Int?,
    @field:Range(min = 1, max = 30)
    val maxLocation: Int?,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
