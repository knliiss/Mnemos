package com.mnemos.account.application.query.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

interface AccountQueryRepository {
    fun findSummariesByUserId(
        userId: UserId,
        activeAccountId: AccountId?,
    ): List<AccountSummary>

    fun findLookupSummariesByAccountIds(accountIds: Set<AccountId>): List<AccountLookupSummary>

    fun findDetailsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
        activeAccountId: AccountId?,
    ): AccountDetails?
}
