package com.mnemos.accountservice.application.query.account.member

import com.mnemos.common.core.model.id.AccountId

interface AccountMemberQueryRepository {
    fun findAllByAccountId(accountId: AccountId): List<AccountMemberListItem>
}
