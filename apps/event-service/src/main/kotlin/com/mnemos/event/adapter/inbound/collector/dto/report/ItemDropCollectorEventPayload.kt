package com.mnemos.event.adapter.inbound.collector.dto.report

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ItemDropCollectorEventPayload(
    @field:NotBlank
    @field:Size(min = 4, max = 24)
    val itemName: String,
    val itemType: DomainItemType,
    val itemRarity: DomainItemRarity,
    @field:NotBlank
    @field:Size(min = 4, max = 20)
    @field:Pattern(
        regexp = "^[\\p{L}0-9_]+$",
    )
    val droppedFor: String,
) : CollectorEventPayload
