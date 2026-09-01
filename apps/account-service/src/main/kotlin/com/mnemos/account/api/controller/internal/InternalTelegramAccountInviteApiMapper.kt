package com.mnemos.account.api.controller.internal

import com.mnemos.account.application.query.account.invite.AccountInviteListItem
import com.mnemos.account.application.query.account.invite.AccountInviteStatus
import com.mnemos.account.application.service.invite.CreateAccountInviteService.CreateAccountInviteResult
import com.mnemos.account.application.service.invite.InternalTelegramAccountInvites
import com.mnemos.contracts.account.internal.v1.CreateInternalTelegramAccountInviteResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInviteResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInviteStatus
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInvitesResponse

object InternalTelegramAccountInviteApiMapper {
    fun InternalTelegramAccountInvites.toResponse(): InternalTelegramAccountInvitesResponse =
        InternalTelegramAccountInvitesResponse(
            accountId = accountId.id,
            cristalixNickname = cristalixNickname.value,
            invites = invites.map(AccountInviteListItem::toResponse),
        )

    fun CreateAccountInviteResult.toResponse(): CreateInternalTelegramAccountInviteResponse =
        CreateInternalTelegramAccountInviteResponse(
            id = id.id,
            code = code.value,
            createdAt = createdAt,
            expiresAt = expiresAt,
        )

    private fun AccountInviteListItem.toResponse(): InternalTelegramAccountInviteResponse =
        InternalTelegramAccountInviteResponse(
            id = id.id,
            code = code.value,
            createdAt = createdAt,
            expiresAt = expiresAt,
            consumedAt = consumedAt,
            revokedAt = revokedAt,
            status = status.toResponse(),
        )

    private fun AccountInviteStatus.toResponse(): InternalTelegramAccountInviteStatus =
        InternalTelegramAccountInviteStatus.valueOf(name)
}
