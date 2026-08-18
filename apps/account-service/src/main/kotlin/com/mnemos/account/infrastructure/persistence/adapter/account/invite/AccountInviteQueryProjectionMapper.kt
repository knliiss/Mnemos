package com.mnemos.account.infrastructure.persistence.adapter.account.invite

import com.mnemos.account.application.query.account.invite.AccountInviteListItem
import com.mnemos.account.application.query.account.invite.AccountInviteStatus
import com.mnemos.account.domain.model.account.invite.InviteCode
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.account.infrastructure.persistence.projection.account.invite.AccountInviteListProjection
import com.mnemos.common.core.model.id.UserId
import java.time.Instant

object AccountInviteQueryProjectionMapper {
    fun AccountInviteListProjection.toListItem(now: Instant): AccountInviteListItem =
        AccountInviteListItem(
            id = InviteId(id),
            code = InviteCode.of(code),
            createdByUserId = UserId(createdByUserId),
            createdAt = createdAt,
            expiresAt = expiresAt,
            consumedAt = consumedAt,
            revokedAt = revokedAt,
            status = resolveStatus(now),
        )

    private fun AccountInviteListProjection.resolveStatus(now: Instant): AccountInviteStatus =
        when {
            consumedAt != null -> AccountInviteStatus.CONSUMED
            revokedAt != null -> AccountInviteStatus.REVOKED
            expiresAt != null && !expiresAt!!.isAfter(now) -> AccountInviteStatus.EXPIRED
            else -> AccountInviteStatus.ACTIVE
        }
}
