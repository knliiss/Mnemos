package com.mnemos.event.contracts.contract.v1

import com.mnemos.event.contracts.shared.ContractItemRarity
import com.mnemos.event.contracts.shared.ContractItemType
import java.util.UUID

data class ItemDroppedV1(
    val itemId: UUID,
    val itemKey: String? = null,
    val itemName: String,
    val itemType: ContractItemType,
    val itemRarity: ContractItemRarity,
    val droppedFor: String,
    val playerId: UUID? = null,
)
