package com.mnemos.event.application.itemdrop.usecase.first

import com.mnemos.event.domain.item.model.DomainItemType
import java.time.Instant

data class FindFirstItemDropQuery(
    val itemName: String,
    val itemType: DomainItemType,
    val from: Instant,
    val to: Instant,
)
