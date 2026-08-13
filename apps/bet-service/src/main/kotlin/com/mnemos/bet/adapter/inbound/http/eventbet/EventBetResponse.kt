package com.mnemos.bet.adapter.inbound.http.eventbet

import com.mnemos.bet.contracts.shared.BetSide
import com.mnemos.bet.domain.eventbet.model.EventBetStatus
import java.util.UUID

data class EventBetResponse(
    val id: UUID,
    val roundId: UUID,
    val side: BetSide,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: EventBetStatus,
)
