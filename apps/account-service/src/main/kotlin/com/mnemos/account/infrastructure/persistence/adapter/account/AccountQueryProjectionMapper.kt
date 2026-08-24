package com.mnemos.account.infrastructure.persistence.adapter.account

import com.mnemos.account.application.query.account.AccountDetails
import com.mnemos.account.application.query.account.AccountLookupSummary
import com.mnemos.account.application.query.account.AccountSummary
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.infrastructure.persistence.projection.account.AccountDetailsProjection
import com.mnemos.account.infrastructure.persistence.projection.account.AccountLookupSummaryProjection
import com.mnemos.account.infrastructure.persistence.projection.account.AccountSummaryProjection
import com.mnemos.common.core.model.id.AccountId

object AccountQueryProjectionMapper {
    fun AccountSummaryProjection.toSummary(activeAccountId: AccountId?): AccountSummary {
        val accountId = AccountId(id)
        val memberId = AccountMemberId(currentMemberId)

        return AccountSummary(
            id = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            currentMemberId = memberId,
            memberCount = Math.toIntExact(memberCount),
            maxMembers = maxMembers,
            owner = memberId == AccountMemberId(ownerMemberId),
            active = accountId == activeAccountId,
            status = AccountStatus.valueOf(status),
        )
    }

    fun AccountLookupSummaryProjection.toLookupSummary(): AccountLookupSummary =
        AccountLookupSummary(
            id = AccountId(id),
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
        )

    fun AccountDetailsProjection.toDetails(activeAccountId: AccountId?): AccountDetails {
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
            status = AccountStatus.valueOf(status),
            baseRoleId = baseRoleId?.let(::RoleId),
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
