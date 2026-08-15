package com.mnemos.account.application.service.role

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.policy.role.RoleAccessGuard
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.ASSIGN_MEMBER_ROLES
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AssignRoleService(
    private val contextProvider: ActiveAccountContextProvider,
    private val roleAccessGuard: RoleAccessGuard,
    private val roleAssignmentRepository: RoleAssignmentRepository,
) {
    @Transactional
    fun assignRole(
        accountId: AccountId,
        memberId: AccountMemberId,
        roleId: RoleId,
    ) {
        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = ASSIGN_MEMBER_ROLES,
            )

        roleAccessGuard.requireCanAssignRole(
            context = context,
            targetMemberId = memberId,
            roleId = roleId,
        )

        val now = Instant.now()
        val assignment =
            RoleAssignment.createManual(
                accountId = accountId,
                accountMemberId = memberId,
                roleId = roleId,
                assignedByUserId = context.user.id,
                now = now,
            )

        roleAssignmentRepository.create(assignment)
    }

    @Transactional
    fun revokeRole(
        accountId: AccountId,
        memberId: AccountMemberId,
        roleId: RoleId,
    ) {
        val context =
            contextProvider.requirePermissionForUpdate(
                accountId = accountId,
                permission = ASSIGN_MEMBER_ROLES,
            )

        roleAccessGuard.requireCanRevokeRole(
            context = context,
            targetMemberId = memberId,
            roleId = roleId,
        )

        roleAssignmentRepository.remove(
            roleId = roleId,
            accountMemberId = memberId,
        )
    }
}
