package com.mnemos.account.application.query.account.member

import com.mnemos.common.core.model.id.AccountId

interface AccountMemberQueryRepository {
    fun findAllByAccountId(accountId: AccountId): List<AccountMemberListItem>
}
