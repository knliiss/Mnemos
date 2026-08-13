package com.mnemos.bet.adapter.inbound.http.itemrace

import com.mnemos.bet.contracts.shared.BetItemType
import com.mnemos.bet.domain.itemrace.model.ItemRaceStatus
import java.time.Instant
import java.util.UUID

data class ItemRaceResponse(
    val id: UUID,
    val itemName: String,
    val itemType: BetItemType,
    val registrationClosesAt: Instant,
    val startsAt: Instant,
    val expiresAt: Instant,
    val entryAmount: Long,
    val feePercent: Int,
    val status: ItemRaceStatus,
    val winnerAccountId: UUID?,
    val participants: List<ItemRaceParticipantResponse> = emptyList(),
)
