package com.mnemos.event.adapter.inbound.collector.dto.report

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ItemDropCollectorEventPayload(
    @field:Size(max = 160)
    @field:Pattern(
        regexp = "^sao\\.item\\.[A-Za-z0-9_.-]+\\.name$",
    )
    val itemKey: String? = null,
    @field:NotBlank
    @field:Size(min = 4, max = 96)
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
