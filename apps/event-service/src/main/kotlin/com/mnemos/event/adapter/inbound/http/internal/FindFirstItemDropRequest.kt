package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.domain.item.model.DomainItemType
import java.time.Instant

data class FindFirstItemDropRequest(
    val itemName: String,
    val itemType: DomainItemType,
    val from: Instant,
    val to: Instant,
)
