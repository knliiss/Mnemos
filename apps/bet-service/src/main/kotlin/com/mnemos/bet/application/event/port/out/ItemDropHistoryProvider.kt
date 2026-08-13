package com.mnemos.bet.application.event.port.out

import com.mnemos.bet.application.event.model.FirstMatchingItemDrop
import com.mnemos.bet.domain.itemrace.model.DomainItemType
import java.time.Instant

interface ItemDropHistoryProvider {
    fun findFirst(
        itemName: String,
        itemType: DomainItemType,
        from: Instant,
        to: Instant,
    ): FirstMatchingItemDrop?
}
