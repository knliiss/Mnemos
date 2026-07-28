package com.mnemos.accountservice.application.query.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId

interface AccountQueryRepository {
    fun findSummariesByUserId(
        userId: UserId,
        activeAccountId: AccountId?,
    ): List<AccountSummary>

    fun findDetailsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
        activeAccountId: AccountId?,
    ): AccountDetails?
}
