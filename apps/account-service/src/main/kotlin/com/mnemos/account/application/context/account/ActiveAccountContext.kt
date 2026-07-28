package com.mnemos.account.application.context.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.user.User

data class ActiveAccountContext(
    val user: User,
    val account: Account,
    val member: AccountMember,
)
