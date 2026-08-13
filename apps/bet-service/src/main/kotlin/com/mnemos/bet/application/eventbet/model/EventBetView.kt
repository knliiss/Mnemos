package com.mnemos.bet.application.eventbet.model

import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBetId
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import com.mnemos.bet.domain.eventbet.model.EventBetStatus
import com.mnemos.common.core.model.id.AccountId

data class EventBetView(
    val id: EventBetId,
    val roundId: EventBetRoundId,
    val accountId: AccountId,
    val side: DomainBetSide,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val status: EventBetStatus,
)
