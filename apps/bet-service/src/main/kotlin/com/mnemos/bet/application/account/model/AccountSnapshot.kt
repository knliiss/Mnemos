package com.mnemos.bet.application.account.model

import com.mnemos.common.core.model.id.AccountId

data class AccountSnapshot(
    val id: AccountId,
    val nickname: String,
)
