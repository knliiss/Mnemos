package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import java.util.UUID

data class AdminEventBetRoundMetadataResponse(
    val roundId: UUID,
    val betCount: Int,
    val bettors: List<AdminEventBetRoundBettorResponse>,
)
