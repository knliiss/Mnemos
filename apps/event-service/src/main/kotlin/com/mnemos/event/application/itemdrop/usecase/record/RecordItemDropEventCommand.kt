package com.mnemos.event.application.itemdrop.usecase.record

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import java.time.Instant

data class RecordItemDropEventCommand(
    val itemKey: String? = null,
    val itemName: String,
    val itemType: DomainItemType,
    val itemRarity: DomainItemRarity,
    val droppedFor: String,
    val occurredAt: Instant,
)
