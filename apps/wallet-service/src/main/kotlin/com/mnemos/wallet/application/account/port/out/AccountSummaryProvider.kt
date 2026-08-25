package com.mnemos.wallet.application.account.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.application.account.model.AccountSummary

interface AccountSummaryProvider {
    fun getByIds(accountIds: Set<AccountId>): Map<AccountId, AccountSummary>
}
