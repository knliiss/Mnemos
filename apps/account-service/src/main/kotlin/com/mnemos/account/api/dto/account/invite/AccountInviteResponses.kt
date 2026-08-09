package com.mnemos.account.api.dto.account.invite

import com.mnemos.account.application.query.account.invite.AccountInviteListItem
import com.mnemos.account.application.query.account.invite.AccountInviteStatus
import com.mnemos.account.application.service.invite.CreateAccountInviteService.CreateAccountInviteResult
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

fun CreateAccountInviteResult.toResponse(): CreateAccountInviteResponse =
    CreateAccountInviteResponse(
        id = id.id,
        code = code.value,
        createdAt = createdAt,
        expiresAt = expiresAt,
    )

fun AccountInviteListItem.toResponse(): AccountInviteListItemResponse =
    AccountInviteListItemResponse(
        id = id.id,
        code = code.value,
        createdByUserId = createdByUserId.id,
        createdAt = createdAt,
        expiresAt = expiresAt,
        consumedAt = consumedAt,
        revokedAt = revokedAt,
        status = status.toResponse(),
    )

private fun AccountInviteStatus.toResponse(): AccountInviteStatusResponse = AccountInviteStatusResponse.valueOf(name)
