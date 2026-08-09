package com.mnemos.event.domain.verification.model.report

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType

data class ItemDropCollectorEventReport(
    val itemName: String,
    val itemType: DomainItemType,
    val droppedFor: String,
    val itemRarity: DomainItemRarity,
) : CollectorEventReport
