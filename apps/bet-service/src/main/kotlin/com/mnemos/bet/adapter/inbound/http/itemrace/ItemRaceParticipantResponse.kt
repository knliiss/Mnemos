package com.mnemos.bet.adapter.inbound.http.itemrace

import com.mnemos.bet.domain.itemrace.model.ItemRaceParticipantStatus
import java.util.UUID

data class ItemRaceParticipantResponse(
    val id: UUID,
    val raceId: UUID,
    val accountId: UUID,
    val accountNickname: String,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: ItemRaceParticipantStatus,
)
