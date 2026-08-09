package com.mnemos.account.infrastructure.persistence.adapter.account

import com.mnemos.account.application.query.account.AccountDetails
import com.mnemos.account.application.query.account.AccountQueryRepository
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.infrastructure.persistence.projection.account.AccountDetailsProjection
import com.mnemos.account.infrastructure.persistence.projection.account.AccountSummaryProjection
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

private fun AccountSummaryProjection.toSummary(activeAccountId: AccountId?): AccountSummary {
    val accountId = AccountId(id)
    val memberId = AccountMemberId(currentMemberId)

    return AccountSummary(
        id = accountId,
        cristalixNickname = cristalixNickname,
        currentMemberId = memberId,
        memberCount = Math.toIntExact(memberCount),
        maxMembers = maxMembers,
        owner = memberId == AccountMemberId(ownerMemberId),
        active = accountId == activeAccountId,
    )
}

private fun AccountDetailsProjection.toDetails(activeAccountId: AccountId?): AccountDetails {
    val accountId = AccountId(id)
    val memberId = AccountMemberId(currentMemberId)

    return AccountDetails(
        id = accountId,
        cristalixUuid = cristalixUuid,
        cristalixNickname = cristalixNickname,
        currentMemberId = memberId,
        memberCount = Math.toIntExact(memberCount),
        maxMembers = maxMembers,
        owner = memberId == AccountMemberId(ownerMemberId),
        active = accountId == activeAccountId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
