package com.mnemos.bet.application.eventbet.usecase.place

import com.mnemos.bet.domain.eventbet.model.DomainBetSide
import com.mnemos.bet.domain.eventbet.model.EventBetRoundId
import java.util.UUID

data class PlaceEventBetCommand(
    val roundId: EventBetRoundId,
    val side: DomainBetSide,
    val amount: Long,
    val idempotencyKey: UUID? = null,
)
