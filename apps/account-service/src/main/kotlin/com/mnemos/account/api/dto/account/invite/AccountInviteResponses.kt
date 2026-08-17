package com.mnemos.account.api.dto.account.invite

import java.time.Instant
import java.util.UUID

data class CreateAccountInviteResponse(
    val id: UUID,
    val code: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
)

data class AccountInviteListItemResponse(
    val id: UUID,
    val code: String,
    val createdByUserId: UUID,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val consumedAt: Instant?,
    val revokedAt: Instant?,
    val status: AccountInviteStatusResponse,
)

enum class AccountInviteStatusResponse {
    ACTIVE,
    CONSUMED,
    REVOKED,
    EXPIRED,
}
