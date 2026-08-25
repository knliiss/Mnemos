package com.mnemos.bet.application.eventbet.model

import com.mnemos.bet.domain.eventbet.model.EventBetRoundId

data class AdminEventBetRoundMetadataView(
    val roundId: EventBetRoundId,
    val betCount: Int,
    val bettors: List<AdminEventBetRoundBettorView>,
)
