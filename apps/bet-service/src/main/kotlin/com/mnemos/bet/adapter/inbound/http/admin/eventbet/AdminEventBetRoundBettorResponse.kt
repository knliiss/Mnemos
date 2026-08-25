package com.mnemos.bet.adapter.inbound.http.admin.eventbet

import java.util.UUID

data class AdminEventBetRoundBettorResponse(
    val accountId: UUID,
    val accountNickname: String?,
)
