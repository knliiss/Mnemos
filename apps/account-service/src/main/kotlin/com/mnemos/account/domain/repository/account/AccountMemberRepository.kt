package com.mnemos.account.domain.repository.account

import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission

interface AccountMemberRepository {
    fun create(member: AccountMember): AccountMember

    fun findByAccountIdAndUserId(
        accountId: AccountId,
        userId: UserId,
    ): AccountMember?

    fun findUserIdsByAccountId(accountId: AccountId): List<UserId>

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
