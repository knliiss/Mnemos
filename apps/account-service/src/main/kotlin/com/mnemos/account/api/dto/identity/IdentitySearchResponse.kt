package com.mnemos.account.api.dto.identity

import java.util.UUID

data class IdentitySearchResponse(
    val userId: UUID,
    val username: String,
    val telegramId: Long,
    val accounts: List<IdentityAccountResponse>,
)

data class IdentityAccountResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val active: Boolean,
)
