package com.mnemos.accountservice.application.context.account

import com.mnemos.accountservice.domain.model.account.Account
import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.user.User

data class ActiveAccountContext(
    val user: User,
    val account: Account,
    val member: AccountMember,
)
