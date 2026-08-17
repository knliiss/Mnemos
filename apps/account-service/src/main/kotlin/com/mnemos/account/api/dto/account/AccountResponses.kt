package com.mnemos.account.api.dto.account

import java.time.Instant
import java.util.UUID

data class CreateAccountResponse(
    val accountId: UUID,
    val memberId: UUID,
)

data class AccountSummaryResponse(
    val id: UUID,
    val cristalixNickname: String,
    val currentMemberId: UUID,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
)

data class AccountDetailsResponse(
    val id: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val currentMemberId: UUID,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
