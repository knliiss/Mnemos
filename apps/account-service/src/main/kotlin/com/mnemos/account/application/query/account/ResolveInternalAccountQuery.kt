package com.mnemos.account.application.query.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

sealed interface ResolveInternalAccountQuery {
    data class ByAccountId(
        val accountId: AccountId,
    ) : ResolveInternalAccountQuery

    data class ByUserId(
        val userId: UserId,
    ) : ResolveInternalAccountQuery

    data class ByCristalixNickname(
        val cristalixNickname: String,
    ) : ResolveInternalAccountQuery
}
