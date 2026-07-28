package com.mnemos.accountservice.infrastructure.persistence.adapter.account

import com.mnemos.accountservice.application.query.account.AccountDetails
import com.mnemos.accountservice.application.query.account.AccountQueryRepository
import com.mnemos.accountservice.application.query.account.AccountSummary
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.infrastructure.persistence.projection.account.AccountDetailsProjection
import com.mnemos.accountservice.infrastructure.persistence.projection.account.AccountSummaryProjection
import com.mnemos.accountservice.infrastructure.persistence.repository.account.AccountQueryEntityRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
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
            .findSummariesByUserId(userId.value)
            .map { it.toSummary(activeAccountId) }

    override fun findDetailsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
        activeAccountId: AccountId?,
    ): AccountDetails? =
        repository
            .findDetailsByAccountIdAndUserId(
                accountId = accountId.value,
                userId = userId.value,
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
