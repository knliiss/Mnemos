package com.mnemos.account.application.query.identity

import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import java.time.Instant
import java.util.UUID

data class AccountIdentityDirectoryEntry(
    val accountId: AccountId,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val status: AccountStatus,
    val ownerUserId: UserId,
    val ownerUsername: String,
    val createdAt: Instant,
)
