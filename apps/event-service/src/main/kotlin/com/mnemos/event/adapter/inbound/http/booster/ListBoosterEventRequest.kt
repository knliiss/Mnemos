package com.mnemos.event.adapter.inbound.http.booster

import com.mnemos.event.domain.booster.model.DomainBoosterType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class ListBoosterEventRequest(
    val type: DomainBoosterType,
    @field:Size(min = 1, max = 16)
    val activatedByName: String?,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
