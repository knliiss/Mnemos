package com.mnemos.accountservice.infrastructure.persistence.adapter.account.invite

import com.mnemos.accountservice.application.query.account.invite.AccountInviteListItem
import com.mnemos.accountservice.application.query.account.invite.AccountInviteQueryRepository
import com.mnemos.accountservice.application.query.account.invite.AccountInviteStatus
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.infrastructure.persistence.projection.account.invite.AccountInviteListProjection
import com.mnemos.accountservice.infrastructure.persistence.repository.account.invite.AccountInviteQueryEntityRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class AccountInviteQueryRepositoryAdapter(
    private val repository: AccountInviteQueryEntityRepository,
) : AccountInviteQueryRepository {
    override fun findAllByAccountId(
        accountId: AccountId,
        now: Instant,
    ): List<AccountInviteListItem> =
        repository
            .findAllByAccountId(accountId.value)
            .map { it.toListItem(now) }
}

private fun AccountInviteListProjection.toListItem(now: Instant): AccountInviteListItem =
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
