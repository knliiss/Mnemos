package com.mnemos.account.api.dto.identity

import com.mnemos.account.domain.model.account.AccountStatus
import java.time.Instant
import java.util.UUID

data class AccountIdentityDirectoryResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val status: AccountStatus,
    val ownerUserId: UUID,
    val ownerUsername: String,
    val createdAt: Instant,
)
