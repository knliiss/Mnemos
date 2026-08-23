package com.mnemos.wallet.application.account.port.out

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

fun interface ActiveAccountProvider {
    fun findActiveAccountId(userId: UserId): AccountId?
}
