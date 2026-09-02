package com.mnemos.account.application.query.account

import com.mnemos.common.core.model.id.AccountId

data class InternalTelegramAccountContext(
    val activeAccountId: AccountId?,
    val accounts: List<AccountSummary>,
)
