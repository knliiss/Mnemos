package com.mnemos.bet.application.account.port.out

import com.mnemos.bet.application.account.model.AccountSummary
import com.mnemos.common.core.model.id.AccountId

interface AccountSummaryProvider {
    fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary>
}
