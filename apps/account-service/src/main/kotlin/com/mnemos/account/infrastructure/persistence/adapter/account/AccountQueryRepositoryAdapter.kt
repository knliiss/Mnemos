package com.mnemos.account.infrastructure.persistence.adapter.account

import com.mnemos.account.application.query.account.AccountDetails
import com.mnemos.account.application.query.account.AccountQueryRepository
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.infrastructure.persistence.adapter.account.AccountQueryProjectionMapper.toDetails
import com.mnemos.account.infrastructure.persistence.adapter.account.AccountQueryProjectionMapper.toSummary
import com.mnemos.account.infrastructure.persistence.repository.account.AccountQueryEntityRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import org.springframework.stereotype.Repository

@Repository
class AccountQueryRepositoryAdapter(
    private val repository: AccountQueryEntityRepository,
) : AccountQueryRepository {
    override fun findSummariesByUserId(
        userId: UserId,
        activeAccountId: AccountId?,
    ): List<AccountSummary> =
        repository
            .findSummariesByUserId(userId.id)
            .map { it.toSummary(activeAccountId) }

    override fun findDetailsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
        activeAccountId: AccountId?,
    ): AccountDetails? =
        repository
            .findDetailsByAccountIdAndUserId(
                accountId = accountId.id,
                userId = userId.id,
            )?.toDetails(activeAccountId)
}
