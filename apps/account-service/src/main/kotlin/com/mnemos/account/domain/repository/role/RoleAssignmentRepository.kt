package com.mnemos.account.domain.repository.role

import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment

interface RoleAssignmentRepository {
    fun create(assignment: RoleAssignment): RoleAssignment

    fun remove(
        roleId: RoleId,
        accountMemberId: AccountMemberId,
    ): Boolean

    fun find(
        roleId: RoleId,
        accountMemberId: AccountMemberId,
    ): RoleAssignment?

    fun findAllByMemberId(accountMemberId: AccountMemberId): List<RoleAssignment>

    fun findAllByRoleId(roleId: RoleId): List<RoleAssignment>

    fun countByRoleId(roleId: RoleId): Int

    fun findMaxRankByMemberId(accountMemberId: AccountMemberId): Long

    fun findMinRankByMemberId(accountMemberId: AccountMemberId): Long

    fun exists(
        accountMemberId: AccountMemberId,
        roleId: RoleId,
    ): Boolean
}
