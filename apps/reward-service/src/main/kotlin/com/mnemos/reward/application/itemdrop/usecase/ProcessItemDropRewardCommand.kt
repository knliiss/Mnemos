package com.mnemos.reward.application.itemdrop.usecase

import com.mnemos.event.contracts.shared.ContractItemRarity
import java.util.UUID

data class ProcessItemDropRewardCommand(
    val eventId: UUID,
    val droppedFor: String,
    val rarity: ContractItemRarity,
)
