package com.mnemos.wallet.application.account.model

import com.mnemos.common.core.model.id.AccountId
import java.util.UUID

data class AccountSummary(
    val accountId: AccountId,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
)
