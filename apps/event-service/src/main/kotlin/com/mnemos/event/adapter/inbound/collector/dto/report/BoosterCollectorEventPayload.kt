package com.mnemos.event.adapter.inbound.collector.dto.report

import com.mnemos.event.domain.booster.model.DomainBoosterType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BoosterCollectorEventPayload(
    val boosterType: DomainBoosterType,
    @field:NotBlank
    @field:Size(min = 4, max = 20)
    val activatedBy: String,
) : CollectorEventPayload
