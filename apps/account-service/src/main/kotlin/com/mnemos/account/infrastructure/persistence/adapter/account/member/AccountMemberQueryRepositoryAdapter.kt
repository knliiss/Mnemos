package com.mnemos.account.infrastructure.persistence.adapter.account.member

import com.mnemos.account.application.query.account.member.AccountMemberListItem
import com.mnemos.account.application.query.account.member.AccountMemberQueryRepository
import com.mnemos.account.infrastructure.persistence.adapter.account.member.AccountMemberQueryProjectionMapper.toListItem
import com.mnemos.account.infrastructure.persistence.repository.account.member.AccountMemberQueryEntityRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository

@Repository
class AccountMemberQueryRepositoryAdapter(
    private val repository: AccountMemberQueryEntityRepository,
) : AccountMemberQueryRepository {
    override fun findAllByAccountId(accountId: AccountId): List<AccountMemberListItem> =
        repository
            .findAllByAccountId(accountId.id)
            .map { it.toListItem() }
}
