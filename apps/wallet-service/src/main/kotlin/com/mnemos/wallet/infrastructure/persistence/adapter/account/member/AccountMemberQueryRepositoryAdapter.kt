package com.mnemos.accountservice.infrastructure.persistence.adapter.account.member

import com.mnemos.accountservice.application.query.account.member.AccountMemberListItem
import com.mnemos.accountservice.application.query.account.member.AccountMemberQueryRepository
import com.mnemos.accountservice.application.query.account.member.AccountMemberRoleSummary
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.accountservice.infrastructure.persistence.projection.account.member.AccountMemberListProjection
import com.mnemos.accountservice.infrastructure.persistence.repository.account.member.AccountMemberQueryEntityRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Repository

@Repository
class AccountMemberQueryRepositoryAdapter(
    private val repository: AccountMemberQueryEntityRepository,
) : AccountMemberQueryRepository {
    override fun findAllByAccountId(accountId: AccountId): List<AccountMemberListItem> =
        repository
            .findAllByAccountId(accountId.value)
            .map { it.toListItem() }
}

private fun AccountMemberListProjection.toListItem(): AccountMemberListItem =
    AccountMemberListItem(
        id = AccountMemberId(id),
        name = name,
        owner = owner,
        highestRole = toHighestRole(),
        joinedAt = joinedAt,
    )

private fun AccountMemberListProjection.toHighestRole(): AccountMemberRoleSummary? {
    val roleId = highestRoleId ?: return null
    val roleName = highestRoleName ?: return null
    val roleColor = highestRoleColor ?: return null

    return AccountMemberRoleSummary(
        id = RoleId(roleId),
        name = AccountRoleName(roleName),
        color = HexColor(roleColor),
    )
}
