package com.mnemos.event.contracts.internal.v1

import com.mnemos.event.contracts.shared.ContractBoosterType
import com.mnemos.event.contracts.shared.ContractGlobalEventType
import com.mnemos.event.contracts.shared.ContractItemRarity
import java.time.Instant
import java.util.UUID

enum class InternalEventFeedType {
    ITEM_DROP,
    BOOSTER,
    GLOBAL,
    RAID,
}

enum class InternalEventFeedSort {
    NEWEST,
    OLDEST,
}

data class InternalEventFeedItemResponse(
    val type: InternalEventFeedType,
    val eventId: UUID,
    val occurredAt: Instant,
    val itemId: UUID? = null,
    val itemKey: String? = null,
    val itemName: String? = null,
    val itemRarity: ContractItemRarity? = null,
    val itemType: String? = null,
    val droppedFor: String? = null,
    val boosterType: ContractBoosterType? = null,
    val activatedBy: String? = null,
    val globalType: ContractGlobalEventType? = null,
    val locations: List<Int> = emptyList(),
)

data class InternalEventFeedResponse(
    val items: List<InternalEventFeedItemResponse>,
    val cursor: UUID?,
)
