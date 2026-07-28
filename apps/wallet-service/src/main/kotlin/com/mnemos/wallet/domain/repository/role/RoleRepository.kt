package com.mnemos.accountservice.domain.repository.role

import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId

interface RoleRepository {
    fun create(role: Role): Role

    fun update(role: Role): Role

    fun deleteById(id: RoleId)

    fun findById(id: RoleId): Role?

    fun existsByAccountIdAndRoleName(
        accountId: AccountId,
        name: AccountRoleName,
    ): Boolean

    fun findByIdAndAccountId(
        roleId: RoleId,
        accountId: AccountId,
    ): Role?

    fun findMinRank(accountId: AccountId): Long

    fun findMaxRank(accountId: AccountId): Long

    fun findAllByAccountIdOrderByRank(accountId: AccountId): List<Role>

    fun updateAll(roles: Collection<Role>): List<Role>

    fun findAllByMemberIdAndAccountIdOrderByRank(
        memberId: AccountMemberId,
        accountId: AccountId,
    ): List<Role>

    fun findAllByAccountId(accountId: AccountId): List<Role>

    fun findAllByMemberId(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): List<Role>
}
