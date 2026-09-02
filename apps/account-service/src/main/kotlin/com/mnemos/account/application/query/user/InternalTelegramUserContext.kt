package com.mnemos.account.application.query.user

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId

data class InternalTelegramUserContext(
    val userId: UserId,
    val activeAccountId: AccountId?,
)
