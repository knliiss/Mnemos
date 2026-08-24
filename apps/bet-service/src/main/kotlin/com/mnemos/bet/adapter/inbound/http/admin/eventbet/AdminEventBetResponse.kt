package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import com.mnemos.bet.contracts.shared.BetSide
import com.mnemos.bet.domain.eventbet.model.EventBetStatus
import java.util.UUID

data class AdminEventBetResponse(
    val id: UUID,
    val roundId: UUID,
    val accountId: UUID,
    val accountNickname: String?,
    val side: BetSide,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: EventBetStatus,
)
