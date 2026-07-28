package com.mnemos.accountservice.domain.repository.account

import com.mnemos.accountservice.domain.model.account.AccountMember
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.UserId

interface AccountMemberRepository {
    fun create(member: AccountMember): AccountMember

    fun findByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): AccountMember?

    fun existsByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): Boolean

    fun countByAccountId(accountId: AccountId): Int

    fun hasEffectivePermission(
        permission: RolePermission,
        memberId: AccountMemberId,
    ): Boolean

    fun deleteById(id: AccountMemberId)

    fun findByIdAndAccountId(
        memberId: AccountMemberId,
        accountId: AccountId,
    ): AccountMember?

    fun findPermissions(memberId: AccountMemberId): Set<RolePermission>

    fun hasPermission(
        memberId: AccountMemberId,
        permission: RolePermission,
    ): Boolean
}
