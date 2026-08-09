package com.mnemos.event.adapter.inbound.http.itemdrop

import com.mnemos.event.domain.item.model.DomainItemRarity
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

data class ListItemDropsRequest(
    val droppedFor: String? = null,
    val itemName: String? = null,
    val itemRarity: DomainItemRarity? = null,
    @field:Min(0)
    val page: Int = 0,
    @field:Min(1)
    @field:Max(100)
    val size: Int = 20,
)
