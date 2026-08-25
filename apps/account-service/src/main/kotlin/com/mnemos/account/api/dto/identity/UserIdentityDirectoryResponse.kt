package com.mnemos.account.api.dto.identity

import java.time.Instant
import java.util.UUID

data class UserIdentityDirectoryResponse(
    val userId: UUID,
    val username: String,
    val telegramId: Long,
    val activeAccountId: UUID?,
    val accountCount: Long,
    val createdAt: Instant,
)
