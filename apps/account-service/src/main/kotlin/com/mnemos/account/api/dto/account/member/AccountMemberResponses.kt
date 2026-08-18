package com.mnemos.account.api.dto.account.member

import java.time.Instant
import java.util.UUID

data class AccountMemberListItemResponse(
    val id: UUID,
    val name: String,
    val owner: Boolean,
    val highestRole: AccountMemberRoleSummaryResponse?,
    val joinedAt: Instant,
)

data class AccountMemberRoleSummaryResponse(
    val id: UUID,
    val name: String,
    val color: String,
)

data class JoinAccountResponse(
    val accountId: UUID,
    val memberId: UUID,
)
