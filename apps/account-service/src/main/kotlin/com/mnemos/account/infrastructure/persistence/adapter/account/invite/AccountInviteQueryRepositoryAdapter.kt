package com.mnemos.account.infrastructure.persistence.adapter.account.invite

import com.mnemos.account.application.query.account.invite.AccountInviteListItem
import com.mnemos.account.application.query.account.invite.AccountInviteQueryRepository
import com.mnemos.account.infrastructure.persistence.adapter.account.invite.AccountInviteQueryProjectionMapper.toListItem
import com.mnemos.account.infrastructure.persistence.repository.account.invite.AccountInviteQueryEntityRepository
import com.mnemos.common.core.model.id.AccountId
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
            .findAllByAccountId(accountId.id)
            .map { it.toListItem(now) }
}
