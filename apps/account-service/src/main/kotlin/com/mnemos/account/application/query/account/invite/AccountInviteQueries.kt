package com.mnemos.account.application.query.account.invite

import com.mnemos.account.domain.model.account.invite.InviteCode
import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.common.core.model.user.UserId
import java.time.Instant

data class AccountInviteListItem(
    val id: InviteId,
    val code: InviteCode,
    val createdByUserId: UserId,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val consumedAt: Instant?,
    val revokedAt: Instant?,
    val status: AccountInviteStatus,
)

enum class AccountInviteStatus {
    ACTIVE,
    CONSUMED,
    REVOKED,
    EXPIRED,
}
