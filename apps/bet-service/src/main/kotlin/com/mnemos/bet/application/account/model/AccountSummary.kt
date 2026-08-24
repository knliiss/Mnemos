package com.mnemos.bet.application.account.model

import com.mnemos.common.core.model.id.AccountId

data class AccountSummary(
    val accountId: AccountId,
    val nickname: String,
)
