package com.mnemos.account.application.query.identity

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.time.Instant

data class UserIdentityDirectoryEntry(
    val userId: UserId,
    val username: String,
    val telegramId: Long,
    val activeAccountId: AccountId?,
    val accountCount: Long,
    val createdAt: Instant,
)
