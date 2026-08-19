package com.mnemos.account.application.query.account

import com.mnemos.common.core.model.id.AccountId

data class InternalAccountBootstrapQuery(
    val afterAccountId: AccountId?,
    val limit: Int,
)
