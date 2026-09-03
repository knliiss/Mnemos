package com.mnemos.account.api.dto.account

import java.time.Instant
import java.util.UUID

data class AdminAccountDetailsResponse(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val status: String,
    val ownerMemberId: UUID,
    val baseRoleId: UUID?,
    val maxMembers: Int,
    val createdAt: Instant,
    val deactivatedAt: Instant?,
    val deletedAt: Instant?,
    val members: List<AdminAccountMemberResponse>,
)

data class AdminAccountMemberResponse(
    val memberId: UUID,
    val userId: UUID,
    val username: String,
    val owner: Boolean,
    val joinedAt: Instant,
)
