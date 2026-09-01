package com.mnemos.contracts.account.internal.v1

import java.time.Instant
import java.util.UUID

data class CreateInternalTelegramAccountInviteRequest(
    val expiresInMinutes: Long? = 1_440,
)

data class CreateInternalTelegramAccountInviteResponse(
    val id: UUID,
    val code: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
)

data class InternalTelegramAccountInvitesResponse(
    val accountId: UUID,
    val cristalixNickname: String,
    val invites: List<InternalTelegramAccountInviteResponse>,
)

data class InternalTelegramAccountInviteResponse(
    val id: UUID,
    val code: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val consumedAt: Instant?,
    val revokedAt: Instant?,
    val status: InternalTelegramAccountInviteStatus,
)

enum class InternalTelegramAccountInviteStatus {
    ACTIVE,
    CONSUMED,
    REVOKED,
    EXPIRED,
}
