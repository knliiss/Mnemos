package com.mnemos.accountservice.application.result.account

import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId

data class CreateAccountResult(
    val accountId: AccountId,
    val memberId: AccountMemberId
)