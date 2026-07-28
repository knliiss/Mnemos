package com.mnemos.account.application.query.account.invite

import com.mnemos.common.core.model.id.AccountId
import java.time.Instant

interface AccountInviteQueryRepository {
    fun findAllByAccountId(
        accountId: AccountId,
        now: Instant,
    ): List<AccountInviteListItem>
}
