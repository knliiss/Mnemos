package com.mnemos.bet.application.eventbet.model

import com.mnemos.common.core.model.id.AccountId

data class AdminEventBetRoundBettorView(
    val accountId: AccountId,
    val accountNickname: String?,
)
